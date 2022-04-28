/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security;

/**
 * title: SignerTypeEnum<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/21 2:33 上午
 */
public enum SignerTypeEnum {
    SM2("SM2签名"),
    OAUTH2("OAUTH2签名"),
    RSA("RSA签名");

    private String value;

    SignerTypeEnum(String value) {
        this.value = value;
    }

}
