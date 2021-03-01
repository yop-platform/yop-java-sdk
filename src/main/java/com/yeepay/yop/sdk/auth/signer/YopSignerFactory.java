/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.yeepay.yop.sdk.security.SignerTypeEnum;

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
 * @since 2021/1/21 2:26 上午
 */
public class YopSignerFactory {
    private static Map<SignerTypeEnum, YopSigner> signerMap = new HashMap<>(4);

    static {
        signerMap.put(SignerTypeEnum.SM2, new YopPKISigner());
        signerMap.put(SignerTypeEnum.OAUTH2, new YopOauth2Signer());
        signerMap.put(SignerTypeEnum.RSA, new YopPKISigner());
    }

    public static void registerSigner(SignerTypeEnum signerType, YopSigner signer) {
        signerMap.put(signerType, signer);
    }

    public static YopSigner getSigner(SignerTypeEnum signerType) {
        return signerMap.get(signerType);
    }
}
