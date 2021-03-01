/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider.loader;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;

import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021-02-25
 */
public interface YopPlatformCredentialsLoader {

    /**
     * 加载平台证书凭证
     * @param appKey 应用
     * @param serialNo 证书序列号
     * @return
     */
    Map<String, YopPlatformCredentials> load(String appKey, String serialNo);

    /**
     * 刷新平台证书凭证
     * @param appKey 应用
     * @param serialNo 证书序列号
     * @return
     */
    Map<String, YopPlatformCredentials> reload(String appKey, String serialNo);
}
