/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;

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

    String YOP_CERT_RSA_DEFAULT_SERIAL_NO = "rsa_default";

    /**
     * 根据应用&证书序列号获取平台凭证
     * @param appKey 应用
     * @param serialNo 证书序列号
     * @return
     */
    YopPlatformCredentials getCredentials(String appKey, String serialNo);


    /**
     * 重新加载平台默认凭证
     */
    default Map<String, YopPlatformCredentials> reload() throws YopClientException {
        return reload("default", "");
    }

    /**
     * 重新加载某个应用的平台凭证
     * @param appKey 指定应用
     * @param serialNo 证书序列号(非必填，可指定加载特定证书)
     */
    Map<String, YopPlatformCredentials> reload(String appKey, String serialNo);
}
