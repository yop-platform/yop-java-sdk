/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2020/1/15 上午11:22
 */
public class YopOauth2Credentials implements YopCredentials<String> {

    private String appKey;

    private String secretKey;

    public YopOauth2Credentials(String appKey, String secretKey) {
        this.appKey = appKey;
        this.secretKey = secretKey;
    }

    @Override
    public String getAppKey() {
        return this.appKey;
    }

    @Override
    public String getCredential() {
        return secretKey;
    }

}
