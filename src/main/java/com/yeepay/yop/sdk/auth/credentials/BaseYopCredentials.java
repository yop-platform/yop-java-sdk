package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.g3.core.yop.sdk.sample.auth.YopCredentials;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/4 16:49
 */
public abstract class BaseYopCredentials implements YopCredentials {


    private static final long serialVersionUID = 4738332056156001261L;

    private final String appKey;

    private final String secretKey;

    public BaseYopCredentials(String appKey, String secretKey) {
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
