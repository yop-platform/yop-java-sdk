/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor.auth.credentials;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2021/12/23 10:53 上午
 */
public class MockEncryptorCredentials implements YopCredentials<MockEncryptorCredentialsItem> {

    private String appKey;

    private MockEncryptorCredentialsItem credentialsItem;

    public MockEncryptorCredentials(String appKey, MockEncryptorCredentialsItem credentialsItem) {
        this.appKey = appKey;
        this.credentialsItem = credentialsItem;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }

    @Override
    public MockEncryptorCredentialsItem getCredential() {
        return credentialsItem;
    }
}
