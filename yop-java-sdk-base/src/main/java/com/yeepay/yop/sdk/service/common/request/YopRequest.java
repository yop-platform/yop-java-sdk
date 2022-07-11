package com.yeepay.yop.sdk.service.common.request;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.yeepay.yop.sdk.YopConstants.TOTAL_ENCRYPT_PARAMS;
import static com.yeepay.yop.sdk.constants.CharacterConstants.DOLLAR;
import static com.yeepay.yop.sdk.utils.JsonUtils.isTotalEncrypt;

/**
 * title: Yop请求<br>
 * description: 用于封装请求参数<br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:14
 */
public class YopRequest extends BaseRequest {

    private static final long serialVersionUID = -1L;

    private String apiUri;

    private String httpMethod;

    private final Multimap<String, String> parameters = ArrayListMultimap.create();

    private final Multimap<String, Object> multipartFiles = ArrayListMultimap.create();

    private Object content;

    public YopRequest(String apiUri, String httpMethod) {
        this.apiUri = apiUri;
        this.httpMethod = httpMethod;
    }

    public YopRequest(String apiUri, String httpMethod, YopRequestConfig requestConfig) {
        super(requestConfig);
        this.apiUri = apiUri;
        this.httpMethod = httpMethod;
    }

    public String getApiUri() {
        return apiUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public Multimap<String, String> getParameters() {
        return parameters;
    }

    public Multimap<String, Object> getMultipartFiles() {
        return multipartFiles;
    }

    public Object getContent() {
        return content;
    }

    public YopRequest addParameter(String name, String value) {
        validateParameter(name, value);
        parameters.put(name, value);
        return this;
    }

    public YopRequest addEncryptParameter(String name, String value) {
        addParameter(name, value);
        getRequestConfig().addEncryptParam(name);
        return this;
    }

    public YopRequest addParameters(String name, List<String> values) {
        validateParameter(name, values);
        parameters.putAll(name, values);
        return this;
    }

    public YopRequest addEncryptParameters(String name, List<String> values) {
        addParameters(name, values);
        getRequestConfig().addEncryptParam(name);
        return this;
    }

    public YopRequest addParameter(String name, Object value) {
        validateParameter(name, value);
        if (value instanceof Collection) {
            for (Object o : (Collection) value) {
                if (o != null) {
                    parameters.put(name, o.toString());
                }
            }
        } else if (value.getClass().isArray()) {
            int len = Array.getLength(value);
            for (int i = 0; i < len; i++) {
                Object o = Array.get(value, i);
                if (o != null) {
                    parameters.put(name, o.toString());
                }
            }
        } else {
            parameters.put(name, value.toString());
        }
        return this;
    }

    public YopRequest addEncryptParameter(String name, Object value) {
        addParameter(name, value);
        getRequestConfig().addEncryptParam(name);
        return this;
    }

    public YopRequest addMutiPartFile(String name, File file) {
        validateParameter(name, file);
        multipartFiles.put(name, file);
        return this;
    }

    public YopRequest addEncryptMutiPartFile(String name, File file) {
        addMutiPartFile(name, file);
        getRequestConfig().addEncryptParam(name);
        return this;
    }

    public YopRequest addMultiPartFile(String name, InputStream inputStream) {
        validateParameter(name, inputStream);
        multipartFiles.put(name, inputStream);
        return this;
    }

    public YopRequest addEncryptMultiPartFile(String name, InputStream inputStream) {
        addMultiPartFile(name, inputStream);
        getRequestConfig().addEncryptParam(name);
        return this;
    }

    public YopRequest setContent(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new YopClientException("content should not be empty");
        }
        this.content = content;
        return this;
    }

    public YopRequest setEncryptContent(String content) {
        setContent(content);
        getRequestConfig().addEncryptParam(DOLLAR).setTotalEncrypt(true);
        return this;
    }

    public YopRequest setEncryptContent(String content, Set<String> jsonPaths) {
        setContent(content);
        boolean totalEncrypt = isTotalEncrypt(jsonPaths);
        getRequestConfig().addEncryptParams(totalEncrypt ? TOTAL_ENCRYPT_PARAMS : jsonPaths)
                .setTotalEncrypt(totalEncrypt);
        return this;
    }

    public YopRequest setStream(InputStream inputStream) {
        if (inputStream == null) {
            throw new YopClientException("inputStream for content should not be null");
        }
        this.content = inputStream;
        return this;
    }

    public YopRequest setEncryptStream(InputStream inputStream) {
        setStream(inputStream);
        getRequestConfig().addEncryptParam(DOLLAR).setTotalEncrypt(true);
        return this;
    }

    public YopRequest withApiUri(String apiUri) {
        this.apiUri = apiUri;
        return this;
    }

    public YopRequest withHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    @Override
    public String getOperationId() {
        return apiUri;
    }
}
