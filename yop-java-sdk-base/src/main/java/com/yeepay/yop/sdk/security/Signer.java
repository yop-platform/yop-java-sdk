/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/29 4:18 下午
 */
public interface Signer {
    /**
     * 签名
     *
     * @param privateKey
     * @param plaintText
     * @return
     */
    byte[] sign(PrivateKey privateKey, byte[] plaintText);

    /**
     * 验签
     *
     * @param publicKey
     * @param plaintText
     * @param signature
     * @return
     */
    boolean verifySign(PublicKey publicKey, byte[] plaintText, byte[] signature);
}
