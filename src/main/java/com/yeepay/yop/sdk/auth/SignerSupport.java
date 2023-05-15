package com.yeepay.yop.sdk.auth;

import com.yeepay.g3.core.yop.sdk.sample.auth.signer.Oauth2Signer;
import com.yeepay.g3.core.yop.sdk.sample.auth.signer.RsaSigner;

import java.util.HashMap;
import java.util.Map;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 16:38
 */
public class SignerSupport {

    private static final Map<String, Signer> SIGNERS = new HashMap<String, Signer>();

    static {
        SIGNERS.put("RSA", new RsaSigner());
        SIGNERS.put("OAUTH2", new Oauth2Signer());
    }

    public static Signer getSigner(String signerType) {
        return SIGNERS.get(signerType);
    }
}
