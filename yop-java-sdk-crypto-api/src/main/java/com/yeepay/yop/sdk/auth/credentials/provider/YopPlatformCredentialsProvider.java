/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;

import java.security.cert.X509Certificate;

/**
 * title: Yop平台凭证提供方<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:27 PM
 */
public interface YopPlatformCredentialsProvider {

    /**
     * 根据应用&证书序列号获取平台凭证
     *
     * @param appKey   应用
     * @param serialNo 证书序列号
     * @return
     */
    YopPlatformCredentials getCredentials(String appKey, String serialNo);

    /**
     * 根据应用&证书序列号获取平台凭证
     *
     * @param appKey     应用
     * @param serialNo   证书序列号
     * @param serverRoot 平台证书请求端点
     * @return
     */
    default YopPlatformCredentials getCredentials(String appKey, String serialNo, String serverRoot) {
        return getCredentials(appKey, serialNo);
    }

    /**
     * 获取指定服务方、环境、端点下平台凭证
     *
     * @param provider   服务方
     * @param env        环境
     * @param appKey     应用
     * @param serialNo   凭证标识
     * @param serverRoot 端点
     * @return YopPlatformCredentials
     */
    default YopPlatformCredentials getCredentials(String provider, String env, String appKey, String serialNo, String serverRoot) {
        return getCredentials(appKey, serialNo, serverRoot);
    }

    /**
     * 获取应用下某类型的最新可用平台凭证
     *
     * @param appKey         应用标识
     * @param credentialType 凭证类型
     * @return YopPlatformCredentials
     */
    YopPlatformCredentials getLatestCredentials(String appKey, String credentialType);

    /**
     * 获取应用下某类型的最新可用平台凭证
     *
     * @param appKey         应用标识
     * @param credentialType 凭证类型
     * @param serverRoot     请求端点
     * @return YopPlatformCredentials
     */
    default YopPlatformCredentials getLatestCredentials(String appKey, String credentialType, String serverRoot) {
        return getLatestCredentials(appKey, credentialType);
    }

    /**
     * 获取应用下某类型的最新可用平台凭证
     *
     * @param provider       服务方
     * @param env            环境
     * @param appKey         应用
     * @param credentialType 凭证类型
     * @param serverRoot     端点
     * @return YopPlatformCredentials
     */
    default YopPlatformCredentials getLatestCredentials(String provider, String env, String appKey, String credentialType, String serverRoot) {
        return getLatestCredentials(appKey, credentialType, serverRoot);
    }

    /**
     * 将应用下平台证书转换并存入加密机
     *
     * @param appKey         应用标识
     * @param credentialType 凭证类型
     * @param certificate    平台证书
     * @return YopPlatformCredentials
     */
    YopPlatformCredentials storeCredentials(String appKey, String credentialType, X509Certificate certificate);

    /**
     * 将应用下平台证书转换并存储
     *
     * @param provider       服务方
     * @param env            环境
     * @param appKey         应用
     * @param credentialType 凭证类型
     * @param certificate    证书
     * @return YopPlatformCredentials
     */
    YopPlatformCredentials storeCredentials(String provider, String env, String appKey, String credentialType, X509Certificate certificate);
}
