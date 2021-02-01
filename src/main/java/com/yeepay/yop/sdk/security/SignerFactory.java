/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security;

import com.yeepay.yop.sdk.security.rsa.RSA2048;
import com.yeepay.yop.sdk.security.sm.SM2;

import java.util.HashMap;
import java.util.Map;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/2/1 5:52 下午
 */
public class SignerFactory {
    private static final Map<DigestAlgEnum, Signer> map;

    static {
        map = new HashMap<DigestAlgEnum, Signer>();
        map.put(DigestAlgEnum.SHA256, new RSA2048());
        map.put(DigestAlgEnum.SM3, new SM2());
    }

    public static Signer getSigner(DigestAlgEnum digestAlgEnum) {
        return map.get(digestAlgEnum);
    }
}
