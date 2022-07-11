package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * title: SDK配置(新版本)<br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class YopFileSdkConfig implements Serializable {

    private static final long serialVersionUID = 2181419124446854272L;

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("server_root")
    private String serverRoot;

    @JsonProperty("yos_server_root")
    private String yosServerRoot;

    @JsonProperty("sandbox_server_root")
    private String sandboxServerRoot;

    private Map<String, List<YopCertConfig>> isvPrivateKeyMap;

    @JsonProperty("http_client")
    private YopHttpClientConfig httpClient;

    @JsonProperty("trust_all_certs")
    private Boolean trustAllCerts;

    private YopProxyConfig proxy;

    private String region;

    @JsonProperty("yop_cert_store")
    private YopCertStore yopCertStore;

    @Deprecated
    private Map<String, List<YopCertConfig>> isvEncryptKeyMap;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
    }

    public String getYosServerRoot() {
        return yosServerRoot;
    }

    public void setYosServerRoot(String yosServerRoot) {
        this.yosServerRoot = yosServerRoot;
    }

    public String getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public void setSandboxServerRoot(String sandboxServerRoot) {
        this.sandboxServerRoot = sandboxServerRoot;
    }

    public List<YopCertConfig> getIsvPrivateKey(String appKey) {
        if (null == isvPrivateKeyMap) {
            return Collections.emptyList();
        }
        return isvPrivateKeyMap.get(appKey);
    }

    @JsonProperty("isv_private_key")
    public void setIsvPrivateKey(YopCertConfig[] isvPrivateKeys) {
        if (null == isvPrivateKeyMap) {
            isvPrivateKeyMap = Maps.newHashMap();
        }
        for (YopCertConfig isvPrivateKey : isvPrivateKeys) {
            String appKey = StringUtils.defaultString(isvPrivateKey.getAppKey(), getAppKey());
            if (isvPrivateKeyMap.containsKey(appKey)) {
                isvPrivateKeyMap.get(appKey).add(isvPrivateKey);
            } else {
                List<YopCertConfig> list = Lists.newLinkedList();
                list.add(isvPrivateKey);
                isvPrivateKeyMap.put(appKey, list);
            }
        }
    }

    public YopHttpClientConfig getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(YopHttpClientConfig httpClient) {
        this.httpClient = httpClient;
    }

    public Boolean getTrustAllCerts() {
        return trustAllCerts;
    }

    public void setTrustAllCerts(Boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
    }

    public YopProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(YopProxyConfig proxy) {
        this.proxy = proxy;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public YopCertStore getYopCertStore() {
        return yopCertStore;
    }

    public void setYopCertStore(YopCertStore yopCertStore) {
        this.yopCertStore = yopCertStore;
    }

    @Deprecated
    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        if (null == isvEncryptKeyMap) {
            return Collections.emptyList();
        }
        return isvEncryptKeyMap.get(appKey);
    }

    @Deprecated
    @JsonProperty("isv_encrypt_key")
    public void setIsvEncryptKey(YopCertConfig[] isvEncryptKeys) {
        if (null == isvEncryptKeyMap) {
            isvEncryptKeyMap = Maps.newHashMap();
        }
        for (YopCertConfig isvEncryptKey : isvEncryptKeys) {
            String appKey = StringUtils.defaultString(isvEncryptKey.getAppKey(), getAppKey());
            if (isvEncryptKeyMap.containsKey(appKey)) {
                isvEncryptKeyMap.get(appKey).add(isvEncryptKey);
            } else {
                List<YopCertConfig> list = Lists.newLinkedList();
                list.add(isvEncryptKey);
                isvEncryptKeyMap.put(appKey, list);
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
