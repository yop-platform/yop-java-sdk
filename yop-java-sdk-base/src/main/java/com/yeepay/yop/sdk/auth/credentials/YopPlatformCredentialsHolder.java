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
 * @author wdc
 * @version 1.0.0
 * @since 2/5/21 11:52 AM
 */
public class YopPlatformCredentialsHolder implements YopPlatformCredentials {

    private static final long serialVersionUID = -1L;

    private String serialNo;
    private String appKey;
    private CredentialsItem credential;

    @Override
    public String getSerialNo() {
        return serialNo;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }

    @Override
    public CredentialsItem getCredential() {
        return credential;
    }

    public YopPlatformCredentialsHolder withSerialNo(String serialNo) {
        this.serialNo = serialNo;
        return this;
    }

    public YopPlatformCredentialsHolder withAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public YopPlatformCredentialsHolder withCredentials(CredentialsItem credential) {
        this.credential = credential;
        return this;
    }
}
