package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.*;

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

    @JsonProperty("preferred_server_roots")
    private List<String> preferredServerRoots;

    @JsonProperty("preferred_yos_server_roots")
    private List<String> preferredYosServerRoots;

    @JsonProperty("aes_secret_key")
    private String aesSecretKey;

    @JsonProperty("yop_public_key")
    private YopCertConfig[] yopPublicKey;

    private Map<String, List<YopCertConfig>> isvPrivateKeyMap;

    @Deprecated
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

    private Map<String, List<YopCertConfig>> isvEncryptKeyMap;

    @JsonProperty("yop_report")
    private YopReportConfig yopReportConfig;

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

    public List<String> getPreferredServerRoots() {
        return preferredServerRoots;
    }

    @JsonProperty("preferred_server_roots")
    public void setPreferredServerRoots(String[] serverRoots) {
        if (null == preferredServerRoots) {
            preferredServerRoots = Lists.newArrayList();
        }
        if (null == serverRoots) {
            return;
        }
        for (String server : serverRoots) {
            if (StringUtils.isBlank(server)) {
                continue;
            }
            preferredServerRoots.add(server.trim());
        }
    }

    public List<String> getPreferredYosServerRoots() {
        return preferredYosServerRoots;
    }

    @JsonProperty("preferred_yos_server_roots")
    public void setPreferredYosServerRoots(String[] yosServerRoots) {
        if (null == preferredYosServerRoots) {
            preferredYosServerRoots = Lists.newArrayList();
        }
        if (null == yosServerRoots) {
            return;
        }
        for (String server : yosServerRoots) {
            if (StringUtils.isBlank(server)) {
                continue;
            }
            preferredYosServerRoots.add(server.trim());
        }
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

    public List<YopCertConfig> getIsvPrivateKey(String appKey) {
        if (null == isvPrivateKeyMap) {
            return Collections.emptyList();
        }
        return isvPrivateKeyMap.get(appKey);
    }

    @JsonProperty("isv_private_key")
    public void setIsvPrivateKey(YopCertConfig[] isvPrivateKeys) {
        if (null == isvPrivateKeyMap) {
            isvPrivateKeyMap = new HashMap<>(16);
        }
        for (YopCertConfig isvPrivateKey : isvPrivateKeys) {
            String appKey = StringUtils.defaultString(isvPrivateKey.getAppKey(), getAppKey());
            if (isvPrivateKeyMap.containsKey(appKey)) {
                isvPrivateKeyMap.get(appKey).add(isvPrivateKey);
            } else {
                isvPrivateKeyMap.put(appKey, new LinkedList<YopCertConfig>() {{
                    add(isvPrivateKey);
                }});
            }
        }
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

    public List<YopCertConfig> getIsvEncryptKey(String appKey) {
        if (null == isvEncryptKeyMap) {
            return Collections.emptyList();
        }
        return isvEncryptKeyMap.get(appKey);
    }

    @JsonProperty("isv_encrypt_key")
    public void setIsvEncryptKey(YopCertConfig[] isvEncryptKeys) {
        if (null == isvEncryptKeyMap) {
            isvEncryptKeyMap = new HashMap<>(16);
        }
        for (YopCertConfig isvEncryptKey : isvEncryptKeys) {
            String appKey = StringUtils.defaultString(isvEncryptKey.getAppKey(), getAppKey());
            if (isvEncryptKeyMap.containsKey(appKey)) {
                isvEncryptKeyMap.get(appKey).add(isvEncryptKey);
            } else {
                isvEncryptKeyMap.put(appKey, new LinkedList<YopCertConfig>() {{
                    add(isvEncryptKey);
                }});
            }
        }
    }

    public YopReportConfig getYopReportConfig() {
        return yopReportConfig;
    }

    public void setYopReportConfig(YopReportConfig yopReportConfig) {
        this.yopReportConfig = yopReportConfig;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
