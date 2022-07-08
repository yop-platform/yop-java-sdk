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
     * 获取应用下某类型的最新可用平台凭证
     *
     * @param appKey         应用标识
     * @param credentialType 凭证类型
     * @return YopPlatformCredentials
     */
    YopPlatformCredentials getLatestCredentials(String appKey, String credentialType);

    /**
     * 将应用下平台证书转换并存入加密机
     *
     * @param appKey         应用标识
     * @param credentialType 凭证类型
     * @param certificate    平台证书
     * @return YopPlatformCredentials
     */
    YopPlatformCredentials storeCredentials(String appKey, String credentialType, X509Certificate certificate);
}
