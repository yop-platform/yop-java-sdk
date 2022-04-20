package com.yeepay.yop.sdk.security;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.security.aes.AES;

import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/24 下午3:28
 */
public class SymmetricEncryptionFactory {

    private static final Map<SymmetricEncryptAlgEnum, Encryption> map;

    static {
        map = Maps.newHashMap();
        map.put(SymmetricEncryptAlgEnum.AES, new AES());
    }

    public static Encryption getSymmetricEncryption(SymmetricEncryptAlgEnum symmetricEncryptAlg) {
        return map.get(symmetricEncryptAlg);
    }
}
