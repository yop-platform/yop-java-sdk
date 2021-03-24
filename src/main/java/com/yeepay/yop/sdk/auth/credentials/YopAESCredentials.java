package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: AES凭证<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/4 16:51
 */
public class YopAESCredentials implements YopCredentials<String> {

    private static final long serialVersionUID = -1L;

    private String appKey;

    private String aesKey;

    public YopAESCredentials(String appKey, String aesKey) {
        this.appKey = appKey;
        this.appKey = aesKey;
    }

    @Override
    public String getAppKey() {
        return this.appKey;
    }

    @Override
    public String getCredential() {
        return this.aesKey;
    }

}
