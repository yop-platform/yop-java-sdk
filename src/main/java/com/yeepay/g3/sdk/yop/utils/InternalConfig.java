package com.yeepay.g3.sdk.yop.utils;

import com.google.common.collect.Sets;
import com.yeepay.g3.sdk.yop.YopServiceException;
import com.yeepay.g3.sdk.yop.config.*;
import com.yeepay.g3.sdk.yop.config.support.BackUpAppSdkConfigManager;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:48
 */
public final class InternalConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalConfig.class);

    public static final String PROTOCOL_VERSION = "yop-auth-v2";

    public static int CONNECT_TIMEOUT = 30000;
    public static int READ_TIMEOUT = 60000;

    public static int MAX_CONN_TOTAL = 200;
    public static int MAX_CONN_PER_ROUTE = 100;

    public static boolean TRUST_ALL_CERTS = false;

    public static ProxyConfig proxy;

    private static YopCircuitBreakerConfig circuitBreakerConfig = YopCircuitBreakerConfig.DEFAULT_CONFIG;

    private static Set<String> retryExceptions = Sets.newHashSet("java.net.UnknownHostException",
            "java.net.ConnectException:No route to host (connect failed)",
            "java.net.ConnectException:Connection refused (Connection refused)",
            "java.net.ConnectException:Connection refused: connect",
            "java.net.SocketTimeoutException:connect timed out",
            "java.net.NoRouteToHostException",
            "org.apache.http.conn.ConnectTimeoutException", "com.yeepay.shade.org.apache.http.conn.ConnectTimeoutException",
            "org.apache.http.conn.HttpHostConnectException", "com.yeepay.shade.org.apache.http.conn.HttpHostConnectException");

    static {
        init();
    }

    private static void init() {
        AppSdkConfig config = AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();
        if (config != null && config.getHttpClientConfig() != null) {
            HttpClientConfig clientConfig = config.getHttpClientConfig();
            if (clientConfig.getConnectTimeout() != null) {
                CONNECT_TIMEOUT = clientConfig.getConnectTimeout();
            }
            if (clientConfig.getReadTimeout() != null) {
                READ_TIMEOUT = clientConfig.getReadTimeout();
            }
            if (clientConfig.getMaxConnTotal() != null) {
                MAX_CONN_TOTAL = clientConfig.getMaxConnTotal();
            }
            if (clientConfig.getMaxConnPerRoute() != null) {
                MAX_CONN_PER_ROUTE = clientConfig.getMaxConnPerRoute();
            }
            proxy = config.getProxy();
            TRUST_ALL_CERTS = config.getTrustAllCerts();

            if (null != clientConfig.getCircuitBreakerConfig()) {
                circuitBreakerConfig = clientConfig.getCircuitBreakerConfig();
            }

            if (CollectionUtils.isNotEmpty(clientConfig.getRetryExceptions())) {
                retryExceptions = clientConfig.getRetryExceptions();
            }
        }

    }

    public static PublicKey getYopPublicKey(CertTypeEnum certType) {
        AppSdkConfig defaultAppSdkConfig = AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();
        if (defaultAppSdkConfig == null) {
            defaultAppSdkConfig = BackUpAppSdkConfigManager.getBackUpConfig();
        }
        return defaultAppSdkConfig.loadYopPublicKey(certType);
    }

    public static PrivateKey getISVPrivateKey(String appKey, CertTypeEnum certType) {
        AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getConfig(appKey);
        if (appSdkConfig == null) {
            throw new YopServiceException("SDKConfig for appKey:" + appKey + " not exist.");
        }
        return appSdkConfig.loadPrivateKey(certType);
    }

    public static PrivateKey getISVPrivateKey(CertTypeEnum certType) {
        AppSdkConfig defaultAppSdkConfig = AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();
        return defaultAppSdkConfig.loadPrivateKey(certType);
    }

    public static YopCircuitBreakerConfig getCircuitBreakerConfig() {
        return circuitBreakerConfig;
    }

    public static Set<String> getRetryExceptions() {
        return retryExceptions;
    }
}
