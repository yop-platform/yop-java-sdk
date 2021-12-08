/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/20 9:56 上午
 */
public class YopPKICredentials implements YopCredentials<PKICredentialsItem> {

    private String appKey;

    private PKICredentialsItem pkiCredentialsItem;

    public YopPKICredentials(String appKey, PKICredentialsItem pkiCredentialsItem) {
        this.appKey = appKey;
        this.pkiCredentialsItem = pkiCredentialsItem;
    }

    @Override

    public String getAppKey() {
        return appKey;
    }

    @Override
    public PKICredentialsItem getCredential() {
        return pkiCredentialsItem;
    }

}
