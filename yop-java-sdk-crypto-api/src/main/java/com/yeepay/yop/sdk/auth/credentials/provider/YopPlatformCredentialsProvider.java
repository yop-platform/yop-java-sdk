/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * title: <br>
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
    YopPlatformCredentials getYopPlatformCredentials(String appKey, String serialNo);

    /**
     * 重新加载默认应用下的所有平台凭证
     */
    Map<String, YopPlatformCredentials> reload() throws YopClientException;

    /**
     * 重新加载某个应用下的平台凭证
     *
     * @param appKey   指定应用
     * @param serialNo 证书序列号(非必填，可指定加载特定证书)
     */
    Map<String, YopPlatformCredentials> reload(String appKey, String serialNo);

    /**
     * 获取应用下某类型的最新可用平台凭证
     *
     * @param appKey 应用标识
     * @param credentialType 凭证类型
     * @return YopPlatformCredentials
     */
    YopPlatformCredentials getLatestAvailable(String appKey, String credentialType);

    /**
     * 存储应用下平台凭证
     *
     * @param certMap 序列号->平台证书
     */
    void storeCerts(Map<String, X509Certificate> certMap);
}
