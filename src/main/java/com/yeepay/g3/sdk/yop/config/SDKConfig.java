package com.yeepay.g3.sdk.yop.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.yeepay.g3.sdk.yop.config.enums.ModeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;

import static com.yeepay.g3.sdk.yop.http.HttpUtils.formatServerRoot;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class SDKConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    @JsonProperty("app_key")
    private String appKey;

    @JsonProperty("aes_secret_key")
    private String aesSecretKey;

    @JsonProperty("server_root")
    private String serverRoot;

    @JsonProperty("yos_server_root")
    private String yosServerRoot;

    @JsonProperty("preferred_server_roots")
    private List<String> preferredServerRoots;

    @JsonProperty("preferred_yos_server_roots")
    private List<String> preferredYosServerRoots;

    @JsonProperty("sandbox_server_root")
    private String sandboxServerRoot;

    @JsonProperty("yop_public_key")
    private CertConfig[] yopPublicKey;

    @JsonProperty("isv_private_key")
    private CertConfig[] isvPrivateKey;

    @JsonProperty("encrypt_key")
    private String encryptKey;

    @Deprecated
    @JsonProperty("connect_timeout")
    private Integer connectTimeout;

    @Deprecated
    @JsonProperty("read_timeout")
    private Integer readTimeout;

    @JsonProperty("http_client")
    private HttpClientConfig httpClient;

    @JsonProperty("trust_all_certs")
    private Boolean trustAllCerts;

    @JsonProperty("default")
    private Boolean defaulted;

    private ModeEnum mode;

    private ProxyConfig proxy;

    @JsonProperty("yop_report")
    private YopReportConfig yopReportConfig;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = formatServerRoot(serverRoot);
    }

    public String getYosServerRoot() {
        return yosServerRoot;
    }

    public List<String> getPreferredServerRoots() {
        return preferredServerRoots;
    }

    @JsonProperty("preferred_server_roots")
    public SDKConfig setPreferredServerRoots(String[] serverRoots) {
        if (null == preferredServerRoots) {
            preferredServerRoots = Lists.newArrayList();
        }
        if (null == serverRoots) {
            return this;
        }
        for (String server : serverRoots) {
            if (StringUtils.isBlank(server)) {
                continue;
            }
            preferredServerRoots.add(formatServerRoot(server));
        }
        return this;
    }

    public List<String> getPreferredYosServerRoots() {
        return preferredYosServerRoots;
    }

    @JsonProperty("preferred_yos_server_roots")
    public SDKConfig setPreferredYosServerRoots(String[] yosServerRoots) {
        if (null == preferredYosServerRoots) {
            preferredYosServerRoots = Lists.newArrayList();
        }
        if (null == yosServerRoots) {
            return this;
        }
        for (String server : yosServerRoots) {
            if (StringUtils.isBlank(server)) {
                continue;
            }
            preferredYosServerRoots.add(formatServerRoot(server));
        }
        return this;
    }

    public String getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public void setSandboxServerRoot(String sandboxServerRoot) {
        if (StringUtils.isNotBlank(sandboxServerRoot)) {
            this.sandboxServerRoot = formatServerRoot(sandboxServerRoot);
        }
    }

    public void setYosServerRoot(String yosServerRoot) {
        this.yosServerRoot = formatServerRoot(yosServerRoot);
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

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
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

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public YopReportConfig getYopReportConfig() {
        return yopReportConfig;
    }

    public void setYopReportConfig(YopReportConfig yopReportConfig) {
        this.yopReportConfig = yopReportConfig;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
