package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("aes_secret_key")
    private String aesSecretKey;

    @JsonProperty("yop_public_key")
    private YopCertConfig[] yopPublicKey;

    @JsonProperty("isv_private_key")
    private YopCertConfig[] isvPrivateKey;

    @JsonProperty("encrypt_key")
    private String encryptKey;

    @JsonProperty("http_client")
    private YopHttpClientConfig httpClient;

    @JsonProperty("trust_all_certs")
    private Boolean trustAllCerts;

    private YopProxyConfig proxy;

    private String region;

    @JsonProperty("yop_cert_store")
    private YopCertStore yopCertStore;

    @JsonProperty("yop_encrypt_key")
    private YopCertConfig[] yopEncryptKey;

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

    public YopCertConfig[] getYopPublicKey() {
        return yopPublicKey;
    }

    public void setYopPublicKey(YopCertConfig[] yopPublicKey) {
        this.yopPublicKey = yopPublicKey;
    }

    public YopCertConfig[] getIsvPrivateKey() {
        return isvPrivateKey;
    }

    public void setIsvPrivateKey(YopCertConfig[] isvPrivateKey) {
        this.isvPrivateKey = isvPrivateKey;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
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

    public YopCertConfig[] getYopEncryptKey() {
        return yopEncryptKey;
    }

    public void setYopEncryptKey(YopCertConfig[] yopEncryptKey) {
        this.yopEncryptKey = yopEncryptKey;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
