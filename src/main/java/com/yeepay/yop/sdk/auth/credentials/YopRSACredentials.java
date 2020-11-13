package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;

/**
 * title: RSA凭证<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/4 16:45
 */
public class YopRSACredentials extends YopBaseCredentials {

    private static final long serialVersionUID = -242895945423391036L;

    private final RSAPrivateKey privateKey;

    private final String encryptKey;

    public YopRSACredentials(String appKey, String secretKey) {
        this(appKey, secretKey, null);
    }

    public YopRSACredentials(String appKey, String secretKey, String encryptKey) {
        super(appKey, null);
        this.privateKey = (RSAPrivateKey) RSAKeyUtils.string2PrivateKey(secretKey);
        this.encryptKey = encryptKey;
    }

    public YopRSACredentials(String appKey, RSAPrivateKey privateKey, String encryptKey) {
        super(appKey, null);
        this.privateKey = privateKey;
        this.encryptKey = encryptKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getEncryptKey() {
        return encryptKey;
    }
}
