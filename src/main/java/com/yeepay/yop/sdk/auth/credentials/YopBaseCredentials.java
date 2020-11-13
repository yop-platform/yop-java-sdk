package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/4 16:49
 */
public abstract class YopBaseCredentials implements YopCredentials {

    private static final long serialVersionUID = 4738332056156001261L;

    private final String appKey;

    private final String secretKey;

    public YopBaseCredentials(String appKey, String secretKey) {
        this.appKey = appKey;
        this.secretKey = secretKey;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }

    @Override
    public String getSecretKey() {
        return secretKey;
    }
}
