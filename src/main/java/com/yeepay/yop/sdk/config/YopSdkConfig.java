package com.yeepay.yop.sdk.config;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.config.provider.file.YopHttpClientConfig;
import com.yeepay.yop.sdk.config.provider.file.YopProxyConfig;
import com.yeepay.yop.sdk.config.provider.file.support.YopCertConfigUtils;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.security.PublicKey;
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
public final class YopSdkConfig implements Serializable {

    private static final long serialVersionUID = 2181419124446854272L;

    private String serverRoot;

    private String yosServerRoot;

    private String sandboxServerRoot;

    private Map<CertTypeEnum, PublicKey> yopPublicKeys;

    private YopHttpClientConfig yopHttpClientConfig;

    private Boolean trustAllCerts;

    private YopProxyConfig proxy;

    private String region;

    private YopCertStore yopCertStore;

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

    public void storeYopPublicKey(YopCertConfig[] yopPublicKeys) {
        this.yopPublicKeys = Maps.newHashMap();
        for (YopCertConfig yopCertConfig : yopPublicKeys) {
            this.yopPublicKeys.put(yopCertConfig.getCertType(), YopCertConfigUtils.loadPublicKey(yopCertConfig));
        }
    }

    public PublicKey loadYopPublicKey(String certType) {
        return this.yopPublicKeys.get(CertTypeEnum.parse(certType));
    }

    public PublicKey loadYopPublicKey(CertTypeEnum certType) {
        return this.yopPublicKeys.get(certType);
    }

    public YopHttpClientConfig getYopHttpClientConfig() {
        return yopHttpClientConfig;
    }

    public void setYopHttpClientConfig(YopHttpClientConfig yopHttpClientConfig) {
        this.yopHttpClientConfig = yopHttpClientConfig;
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
