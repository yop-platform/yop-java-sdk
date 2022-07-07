/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security;

/**
 * title: DigestAlgEnum<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/12/22 4:00 下午
 */
public enum DigestAlgEnum {

    SHA256("sha-256摘要"),
    SM3("SM3摘要算法");

    private final String value;

    DigestAlgEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
