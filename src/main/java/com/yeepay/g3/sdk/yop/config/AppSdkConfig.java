package com.yeepay.g3.sdk.yop.config;

import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.config.enums.ModeEnum;
import com.yeepay.g3.sdk.yop.config.support.ConfigUtils;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.yeepay.g3.sdk.yop.config.YopReportConfig.DEFAULT_YOP_REPORT_CONFIG;
import static com.yeepay.g3.sdk.yop.http.HttpUtils.formatServerRoot;

/**
 * title: 应用SDKConfig<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/2/8 15:32
 */
public class AppSdkConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    private String appKey;

    private String serverRoot;

    private String yosServerRoot;

    private List<String> preferredServerRoots;

    private List<String> preferredYosServerRoots;

    private String sandboxServerRoot;

    private String aesSecretKey;

    private PublicKey defaultYopPublicKey;

    private PrivateKey defaultIsvPrivateKey;

    private String encryptKey;

    private HttpClientConfig httpClientConfig;

    private Map<CertTypeEnum, PublicKey> yopPublicKeys;

    private Map<CertTypeEnum, PrivateKey> isvPrivateKeys;

    private ProxyConfig proxy;

    private ModeEnum mode;

    private boolean trustAllCerts;

    private YopReportConfig yopReportConfig = DEFAULT_YOP_REPORT_CONFIG;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public AppSdkConfig withAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String getServerRoot() {
        return serverRoot;
    }

    public void setServerRoot(String serverRoot) {
        this.serverRoot = formatServerRoot(serverRoot);
    }

    public AppSdkConfig withServerRoot(String serverRoot) {
        setServerRoot(serverRoot);
        return this;
    }

    public String getYosServerRoot() {
        return yosServerRoot;
    }

    public void setYosServerRoot(String yosServerRoot) {
        this.yosServerRoot = formatServerRoot(yosServerRoot);
    }

    public AppSdkConfig withYosServerRot(String yosServerRoot) {
        setYosServerRoot(yosServerRoot);
        return this;
    }

    public List<String> getPreferredServerRoots() {
        return preferredServerRoots;
    }

    public void setPreferredServerRoots(List<String> preferredServerRoots) {
        this.preferredServerRoots = formatServerRoots(preferredServerRoots);
    }

    private List<String> formatServerRoots(List<String> serverRoots) {
        if (CollectionUtils.isNotEmpty(serverRoots)) {
            List<String> formattedServerRoots = new ArrayList<>(serverRoots.size());
            for (String serverRoot : serverRoots) {
                if (StringUtils.isBlank(serverRoot)) {
                    continue;
                }
                formattedServerRoots.add(formatServerRoot(serverRoot));
            }
            return formattedServerRoots;
        }
        return Collections.emptyList();
    }

    public AppSdkConfig withPreferredServerRoots(List<String> preferredServerRoots) {
        setPreferredServerRoots(preferredServerRoots);
        return this;
    }

    public List<String> getPreferredYosServerRoots() {
        return preferredYosServerRoots;
    }

    public void setPreferredYosServerRoots(List<String> preferredYosServerRoots) {
        this.preferredYosServerRoots = formatServerRoots(preferredYosServerRoots);
    }

    public AppSdkConfig withPreferredYosServerRoots(List<String> preferredYosServerRoots) {
        setPreferredYosServerRoots(preferredYosServerRoots);
        return this;
    }

    public String getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public void setSandboxServerRoot(String sandboxServerRoot) {
        this.sandboxServerRoot = formatServerRoot(sandboxServerRoot);
    }

    public AppSdkConfig withSandboxServerRoot(String sandboxServerRoot) {
        setSandboxServerRoot(sandboxServerRoot);
        return this;
    }

    public String getAesSecretKey() {
        return aesSecretKey;
    }

    public void setAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
    }

    public AppSdkConfig withAesSecretKey(String aesSecretKey) {
        this.aesSecretKey = aesSecretKey;
        return this;
    }

    public PublicKey getDefaultYopPublicKey() {
        return defaultYopPublicKey;
    }

    public void setDefaultYopPublicKey(PublicKey defaultYopPublicKey) {
        this.defaultYopPublicKey = defaultYopPublicKey;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public void setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
    }

    public AppSdkConfig withEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
        return this;
    }

    public PrivateKey getDefaultIsvPrivateKey() {
        return defaultIsvPrivateKey;
    }

    public void setDefaultIsvPrivateKey(PrivateKey defaultIsvPrivateKey) {
        this.defaultIsvPrivateKey = defaultIsvPrivateKey;
    }

    public HttpClientConfig getHttpClientConfig() {
        return httpClientConfig;
    }

    public void setHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
    }

    public AppSdkConfig withHttpClientConfig(HttpClientConfig httpClientConfig) {
        this.httpClientConfig = httpClientConfig;
        return this;
    }

    public void storeYopPublicKey(CertConfig[] yopPublicKeys) {
        this.defaultYopPublicKey = ConfigUtils.loadPublicKey(yopPublicKeys[0]);
        this.yopPublicKeys = Maps.newHashMap();
        this.yopPublicKeys.put(yopPublicKeys[0].getCertType(), this.defaultYopPublicKey);
        for (int i = 1; i < yopPublicKeys.length; i++) {
            this.yopPublicKeys.put(yopPublicKeys[i].getCertType(), ConfigUtils.loadPublicKey(yopPublicKeys[i]));
        }
    }

    public void storeIsvPrivateKey(CertConfig[] isvPrivateKeys) {
        this.defaultIsvPrivateKey = ConfigUtils.loadPrivateKey(isvPrivateKeys[0]);
        this.isvPrivateKeys = Maps.newHashMap();
        this.isvPrivateKeys.put(isvPrivateKeys[0].getCertType(), this.defaultIsvPrivateKey);
        for (int i = 1; i < isvPrivateKeys.length; i++) {
            this.isvPrivateKeys.put(isvPrivateKeys[i].getCertType(), ConfigUtils.loadPrivateKey(isvPrivateKeys[i]));
        }
    }

    public PublicKey loadYopPublicKey(CertTypeEnum certType) {
        return this.yopPublicKeys.get(certType);
    }

    public PrivateKey loadPrivateKey(CertTypeEnum certType) {
        return this.isvPrivateKeys.get(certType);
    }

    public ProxyConfig getProxy() {
        return proxy;
    }

    public void setProxy(ProxyConfig proxy) {
        this.proxy = proxy;
    }

    public AppSdkConfig withProxy(ProxyConfig proxy) {
        this.proxy = proxy;
        return this;
    }

    public ModeEnum getMode() {
        return mode;
    }

    public void setMode(ModeEnum mode) {
        this.mode = mode;
    }

    public AppSdkConfig withMode(ModeEnum mode) {
        this.mode = mode;
        return this;
    }

    public boolean getTrustAllCerts() {
        return trustAllCerts;
    }

    public void setTrustAllCerts(boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
    }

    public AppSdkConfig withTrustAllCerts(boolean trustAllCerts) {
        this.trustAllCerts = trustAllCerts;
        return this;
    }

    public YopReportConfig getYopReportConfig() {
        return yopReportConfig;
    }

    public void setYopReportConfig(YopReportConfig yopReportConfig) {
        if (null != yopReportConfig) {
            this.yopReportConfig = yopReportConfig;
        }
    }

    public AppSdkConfig withYopReportConfig(YopReportConfig yopReportConfig) {
        setYopReportConfig(yopReportConfig);
        return this;
    }

    public static final class Builder {
        private SDKConfig sdkConfig;

        public Builder() {
        }

        public static Builder anAppSdkConfig() {
            return new Builder();
        }

        public Builder withSDKConfig(SDKConfig sdkConfig) {
            this.sdkConfig = sdkConfig;
            return this;
        }

        public AppSdkConfig build() {
            AppSdkConfig appSdkConfig = new AppSdkConfig()
                    .withAppKey(sdkConfig.getAppKey())
                    .withAesSecretKey(sdkConfig.getAesSecretKey())
                    .withServerRoot(StringUtils.defaultIfBlank(sdkConfig.getServerRoot(), YopConstants.DEFAULT_SERVER_ROOT))
                    .withYosServerRot(StringUtils.defaultIfBlank(sdkConfig.getYosServerRoot(), YopConstants.DEFAULT_YOS_SERVER_ROOT))
                    .withSandboxServerRoot(StringUtils.defaultIfBlank(sdkConfig.getSandboxServerRoot(), YopConstants.DEFAULT_SANDBOX_SERVER_ROOT))
                    .withPreferredServerRoots(sdkConfig.getPreferredServerRoots())
                    .withPreferredYosServerRoots(sdkConfig.getPreferredYosServerRoots())
                    .withEncryptKey(sdkConfig.getEncryptKey())
                    .withHttpClientConfig(sdkConfig.getHttpClient())
                    .withProxy(sdkConfig.getProxy())
                    .withMode(sdkConfig.getMode())
                    .withTrustAllCerts(BooleanUtils.isTrue(sdkConfig.getTrustAllCerts()))
                    .withYopReportConfig(sdkConfig.getYopReportConfig());
            if (sdkConfig.getYopPublicKey() != null && sdkConfig.getYopPublicKey().length >= 1) {
                appSdkConfig.storeYopPublicKey(sdkConfig.getYopPublicKey());
            }
            if (sdkConfig.getIsvPrivateKey() != null && sdkConfig.getIsvPrivateKey().length >= 1) {
                appSdkConfig.storeIsvPrivateKey(sdkConfig.getIsvPrivateKey());
            }
            return appSdkConfig;
        }
    }
}
