package com.yeepay.yop.sdk.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yeepay.g3.core.yop.sdk.sample.config.enums.ModeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

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
public final class SDKConfig implements Serializable {

    private static final long serialVersionUID = 2181419124446854272L;

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("server_root")
    private String serverRoot;

    @JsonProperty("yos_server_root")
    private String yosServerRoot;

    @JsonProperty("sandbox_server_root")
    private String sandboxServerRoot;

    @JsonProperty("aes_secret_key")
    private String aesSecretKey;

    @JsonProperty("yop_public_key")
    private CertConfig[] yopPublicKey;

    @JsonProperty("isv_private_key")
    private CertConfig[] isvPrivateKey;

    @JsonProperty("encrypt_key")
    private String encryptKey;

    @JsonProperty("http_client")
    private HttpClientConfig httpClient;

    @JsonProperty("trust_all_certs")
    private Boolean trustAllCerts;

    private ProxyConfig proxy;

    private String region;

    @JsonProperty("default")
    private Boolean defaulted;

    private ModeEnum mode;

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

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public CertConfig[] getYopPublicKey() {
        return yopPublicKey;
    }

    public void setYopPublicKey(CertConfig[] yopPublicKey) {
        this.yopPublicKey = yopPublicKey;
    }

    public CertConfig[] getIsvPrivateKey() {
        return isvPrivateKey;
    }

    public void setIsvPrivateKey(CertConfig[] isvPrivateKey) {
        this.isvPrivateKey = isvPrivateKey;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public HttpClientConfig getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClientConfig httpClient) {
        this.httpClient = httpClient;
    }

    public Boolean getTrustAllCerts() {
        return trustAllCerts;
    }

    public void setTrustAllCerts(Boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Boolean getDefaulted() {
        return defaulted;
    }

    public void setDefaulted(Boolean defaulted) {
        this.defaulted = defaulted;
    }

    public ModeEnum getMode() {
        return mode;
    }

    public void setMode(ModeEnum mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
