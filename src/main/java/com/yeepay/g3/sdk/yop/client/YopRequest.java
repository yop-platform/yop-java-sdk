package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.yeepay.g3.sdk.yop.YopServiceException;
import com.yeepay.g3.sdk.yop.config.AppSdkConfig;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.support.BackUpAppSdkConfigManager;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.utils.Assert;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;

/**
 * <pre>
 * 每个请求对应一个ClientRequest对象
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopRequest.class);

    private String signAlg = YopConstants.ALG_SHA256;

    private Multimap<String, String> paramMap = ArrayListMultimap.create();

    private Map<String, Object> multipartFiles = Maps.newHashMap();

    private Map<String, String> headers = new HashMap<String, String>();

    private List<String> ignoreSignParams = new ArrayList<String>(Arrays.asList(YopConstants.SIGN));

    /**
     * app对应的sdkConfig
     */
    private final AppSdkConfig appSdkConfig;

    /**
     * 可支持不同请求使用不同的appKey及secretKey,secretKey只用于本地签名，不会被提交
     */
    private final String secretKey;

    /**
     * 请求是否需要加密
     */
    private Boolean needEncrypt;

    /**
     * 加密密钥
     */
    private String encryptKey;

    public YopRequest() {
        this.appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();
        if (this.appSdkConfig == null) {
            throw new YopServiceException("Default SDKConfig not found.");
        }
        this.secretKey = null;
        init();
    }

    public YopRequest(String appKey) {
        Validate.notBlank(appKey, "AppKey is blank.");
        this.appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getConfig(appKey);
        if (this.appSdkConfig == null) {
            throw new YopServiceException("SDKConfig for appKey:" + appKey + " not found.");
        }
        this.secretKey = null;
        init();
    }

    /**
     * 同一个工程内部可支持多个开放应用发起调用
     */
    public YopRequest(String appKey, String secretKey) {
        Validate.notBlank(appKey, "AppKey is blank.");
        Validate.notBlank(secretKey, "SecretKey is blank.");

        this.appSdkConfig = new AppSdkConfig();
        this.appSdkConfig.setAppKey(appKey);

        AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getConfigWithDefault(appKey);
        if (appSdkConfig == null) {
            appSdkConfig = BackUpAppSdkConfigManager.getBackUpConfig();
        }
        this.appSdkConfig.setServerRoot(appSdkConfig.getServerRoot());
        this.appSdkConfig.setYosServerRoot(appSdkConfig.getYosServerRoot());
        this.appSdkConfig.setSandboxServerRoot(appSdkConfig.getSandboxServerRoot());
        this.appSdkConfig.setDefaultYopPublicKey(appSdkConfig.getDefaultYopPublicKey());
        this.secretKey = secretKey;
        init();
    }

    private void init() {
        headers.put(Headers.YOP_SDK_VERSION, YopConstants.CLIENT_VERSION);
        headers.put(Headers.USER_AGENT, YopConstants.USER_AGENT);
        headers.put(Headers.YOP_APP_KEY, this.appSdkConfig.getAppKey());
    }

    public YopRequest setSubCustomerId(String subCustomerId) {
        headers.put(Headers.YOP_SUB_CUSTOMER_ID, subCustomerId);
        return this;
    }

    public YopRequest setParam(String paramName, Object paramValue) {
        removeParam(paramName);
        addParam(paramName, paramValue, false);
        return this;
    }

    public YopRequest addParam(String paramName, Object paramValue) {
        addParam(paramName, paramValue, false);
        return this;
    }

    /**
     * 添加参数
     *
     * @param paramName  参数名
     * @param paramValue 参数值：如果为集合或数组类型，则自动拆解，最终想作为数组提交到服务端
     * @param ignoreSign 是否忽略签名
     * @return
     */
    public YopRequest addParam(String paramName, Object paramValue, boolean ignoreSign) {
        Assert.hasText(paramName, "参数名不能为空");
        if (paramValue == null || ((paramValue instanceof String) && StringUtils.isBlank((String) paramValue))
                || ((paramValue instanceof Collection<?>) && ((Collection<?>) paramValue).isEmpty())) {
            LOGGER.warn("param " + paramName + "is null or empty，ignore it");
            return this;
        }

        // file
        if (StringUtils.equals("_file", paramName)) {
            this.addFile(paramValue);
            return this;
        }

        if (YopConstants.isProtectedKey(paramName)) {
            paramMap.put(paramName, paramValue.toString());
            return this;
        }
        if (paramValue instanceof Collection<?>) {
            // 集合类
            for (Object o : (Collection<?>) paramValue) {
                if (o != null) {
                    paramMap.put(paramName, o.toString());
                }
            }
        } else if (paramValue.getClass().isArray()) {
            // 数组
            int len = Array.getLength(paramValue);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(paramValue, i);
                if (o != null) {
                    paramMap.put(paramName, o.toString());
                }
            }
        } else {
            paramMap.put(paramName, paramValue.toString());
        }

        if (ignoreSign) {
            ignoreSignParams.add(paramName);
        }
        return this;
    }

    public List<String> getParam(String key) {
        return (List<String>) paramMap.get(key);
    }

    public String getParamValue(String key) {
        return StringUtils.join(paramMap.get(key), ",");
    }

    public String removeParam(String key) {
        return StringUtils.join(paramMap.removeAll(key), ",");
    }

    public Multimap<String, String> getParams() {
        return paramMap;
    }

    public YopRequest addFile(Object file) {
        addFile("_file", file);
        return this;
    }

    public YopRequest addFile(String paramName, Object file) {
        if (file instanceof String || file instanceof File || file instanceof InputStream) {
            multipartFiles.put(paramName, file);
        } else {
            throw new YopClientException("Unsupported file object.");
        }
        return this;
    }

    public Map<String, Object> getMultipartFiles() {
        return multipartFiles;
    }

    public boolean hasFiles() {
        return null != multipartFiles && multipartFiles.size() > 0;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * customize header,but none system reserved headers
     *
     * @param name
     * @param value
     */
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getRequestId() {
        return headers.get(Headers.YOP_REQUEST_ID);
    }

    public void setRequestId(String requestId) {
        headers.put(Headers.YOP_REQUEST_ID, requestId);
    }

    public void setRequestSource(String source) {
        headers.put(Headers.YOP_REQUEST_SOURCE, source);
    }

    public List<String> getIgnoreSignParams() {
        return ignoreSignParams;
    }

    @Deprecated
    public String getSignAlg() {
        return signAlg;
    }

    @Deprecated
    public void setSignAlg(String signAlg) {
        this.signAlg = signAlg;
    }

    @Deprecated
    public String getSecretKey() {
        return secretKey;
    }

    @Deprecated
    public String getAesSecretKey() {
        return secretKey == null ? appSdkConfig.getAesSecretKey() : secretKey;
    }

    public Boolean isNeedEncrypt() {
        return needEncrypt;
    }

    public void setNeedEncrypt(Boolean needEncrypt) {
        this.needEncrypt = needEncrypt;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public AppSdkConfig getAppSdkConfig() {
        return appSdkConfig;
    }

    /**
     * 将参数转换成k=v拼接的形式
     *
     * @return
     */
    public String toQueryString() {
        StringBuilder builder = new StringBuilder();
        for (String key : this.paramMap.keySet()) {
            Collection<String> values = this.paramMap.get(key);
            for (String value : values) {
                builder.append(builder.length() == 0 ? "" : "&");
                builder.append(key);
                builder.append("=");
                builder.append(value);
            }
        }
        return builder.toString();
    }
}
