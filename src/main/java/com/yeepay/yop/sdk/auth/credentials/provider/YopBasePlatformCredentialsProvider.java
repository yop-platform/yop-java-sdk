/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.util.Map;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/4/27 10:03 上午
 */
public abstract class YopBasePlatformCredentialsProvider implements YopPlatformCredentialsProvider {
    @Override
    public Map<String, YopPlatformCredentials> reload() throws YopClientException {
        return reload("default", "");
    }
}
