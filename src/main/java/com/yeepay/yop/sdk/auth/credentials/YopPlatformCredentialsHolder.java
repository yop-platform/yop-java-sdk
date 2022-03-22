/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/5/21 11:52 AM
 */
public class YopPlatformCredentialsHolder implements YopPlatformCredentials {

    private static final long serialVersionUID = -1L;

    private String serialNo;
    private Map<CertTypeEnum, PublicKey> credentialsMap = new LinkedHashMap();

    @Override
    public String getSerialNo() {
        return serialNo;
    }

    @Override
    public PublicKey getPublicKey(CertTypeEnum certType) {
        return credentialsMap.get(certType);
    }

    public YopPlatformCredentialsHolder withSerialNo(String serialNo) {
        this.serialNo = serialNo;
        return this;
    }

    public YopPlatformCredentialsHolder withPublicKey(CertTypeEnum certType, PublicKey publicKey) {
        credentialsMap.put(certType, publicKey);
        return this;
    }

    public YopPlatformCredentialsHolder withPublicKeys(Map<CertTypeEnum, PublicKey> publicKeys) {
        credentialsMap.putAll(publicKeys);
        return this;
    }
}
