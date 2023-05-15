package com.yeepay.yop.sdk.config;

import com.google.common.collect.Maps;
import com.yeepay.g3.core.yop.sdk.sample.YopConstants;
import com.yeepay.g3.core.yop.sdk.sample.config.enums.ModeEnum;
import com.yeepay.g3.core.yop.sdk.sample.config.support.ConfigUtils;
import com.yeepay.g3.core.yop.sdk.sample.security.CertTypeEnum;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

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

    private String sandboxServerRoot;

    private String aesSecretKey;

    private PublicKey defaultYopPublicKey;

    private PrivateKey defaultIsvPrivateKey;

    private HttpClientConfig httpClientConfig;

    private String encryptKey;

    private Map<CertTypeEnum, PublicKey> yopPublicKeys;

    private Map<CertTypeEnum, PrivateKey> isvPrivateKeys;

    private ProxyConfig proxy;

    private String region;

    private ModeEnum mode;

    private boolean trustAllCerts;

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
        this.serverRoot = serverRoot;
    }

    public AppSdkConfig withServerRoot(String serverRoot) {
        this.serverRoot = serverRoot;
        return this;
    }

    public String getYosServerRoot() {
        return yosServerRoot;
    }

    public void setYosServerRoot(String yosServerRoot) {
        this.yosServerRoot = yosServerRoot;
    }

    public AppSdkConfig withYosServerRot(String yosServerRoot) {
        this.yosServerRoot = yosServerRoot;
        return this;
    }

    public String getSandboxServerRoot() {
        return sandboxServerRoot;
    }

    public void setSandboxServerRoot(String sandboxServerRoot) {
        this.sandboxServerRoot = sandboxServerRoot;
    }

    public AppSdkConfig withSandboxServerRoot(String sandboxServerRoot) {
        this.sandboxServerRoot = sandboxServerRoot;
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

    public PublicKey getDefaultYopPublicKey() {
        return defaultYopPublicKey;
    }

    public void setDefaultYopPublicKey(PublicKey defaultYopPublicKey) {
        this.defaultYopPublicKey = defaultYopPublicKey;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public AppSdkConfig withRegion(String region) {
        this.region = region;
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
                    .withEncryptKey(sdkConfig.getEncryptKey())
                    .withHttpClientConfig(sdkConfig.getHttpClient())
                    .withProxy(sdkConfig.getProxy())
                    .withRegion(sdkConfig.getRegion())
                    .withMode(sdkConfig.getMode())
                    .withTrustAllCerts(BooleanUtils.isTrue(sdkConfig.getTrustAllCerts()));
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
