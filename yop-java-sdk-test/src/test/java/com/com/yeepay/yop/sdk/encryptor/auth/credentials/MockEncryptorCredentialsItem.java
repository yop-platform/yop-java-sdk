/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.com.yeepay.yop.sdk.encryptor.auth.credentials;

import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.security.CertTypeEnum;

/**
 * title: 加密机凭证项<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2021/12/23 10:19 上午
 */
public class MockEncryptorCredentialsItem implements CredentialsItem {

    /**
     * 加密机中的密钥标识(mock情况下为base64编码的key)
     */
    private String encryptorCertKey;

    private CertTypeEnum certTypeEnum;

    public MockEncryptorCredentialsItem(String encryptorCertKey, CertTypeEnum certTypeEnum) {
        this.encryptorCertKey = encryptorCertKey;
        this.certTypeEnum = certTypeEnum;
    }

    public String getEncryptorCertKey() {
        return encryptorCertKey;
    }

    @Override
    public CertTypeEnum getCertType() {
        return certTypeEnum;
    }
}
