package com.yeepay.yop.sdk.security;

import com.yeepay.g3.core.yop.sdk.sample.security.aes.AES;

import java.util.HashMap;
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

    private static Map<SymmetricEncryptAlgEnum, SymmetricEncryption> map;

    static {
        map = new HashMap<SymmetricEncryptAlgEnum, SymmetricEncryption>();
        map.put(SymmetricEncryptAlgEnum.AES, new AES());
    }

    public static SymmetricEncryption getSymmetricEncryption(SymmetricEncryptAlgEnum symmetricEncryptAlg) {
        return map.get(symmetricEncryptAlg);
    }
}
