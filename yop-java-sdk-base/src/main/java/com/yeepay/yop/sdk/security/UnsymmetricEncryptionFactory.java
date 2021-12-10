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
 * @since 2021/1/29 4:02 下午
 */
public class UnsymmetricEncryptionFactory {

    private static final Map<DigestAlgEnum, Encryption> map;

    static {
        map = new HashMap<DigestAlgEnum, Encryption>();
        map.put(DigestAlgEnum.SHA256, new RSA2048());
        map.put(DigestAlgEnum.SM3, new SM2());
    }

    public static Encryption getUnsymmetricEncryption(DigestAlgEnum digestAlgEnum) {
        return map.get(digestAlgEnum);
    }
}
