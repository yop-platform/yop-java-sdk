package com.yeepay.yop.sdk.auth;

/**
 * title: 不需要sign的证书<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/4/3 19:26
 */
public class YopCredentialsWithoutSign implements YopCredentials {

    private final String appKey;

    public YopCredentialsWithoutSign(String appKey) {
        this.appKey = appKey;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }

    @Override
    public String getSecretKey() {
        return null;
    }
}
