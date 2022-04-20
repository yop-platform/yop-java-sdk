/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.Encodes;

import java.io.UnsupportedEncodingException;

/**
 * title: 加解密适配器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public abstract class YopEncryptorAdaptor implements YopEncryptor {

    @Override
    public String encryptToBase64(String plain, EncryptOptions options) {
        try {
            return Encodes.encodeUrlSafeBase64(encrypt(plain.getBytes(YopConstants.DEFAULT_ENCODING), options));
        } catch (UnsupportedEncodingException e) {
            throw new YopClientException("error happened when encrypt data", e);
        }
    }

    @Override
    public String encryptToBase64(byte[] plain, EncryptOptions options) {
        return Encodes.encodeUrlSafeBase64(encrypt(plain, options));
    }

    @Override
    public String decryptFromBase64(String cipher, EncryptOptions options) {
        return decryptToPlain(Encodes.decodeBase64(cipher), options);
    }

    @Override
    public String decryptToPlain(byte[] cipher, EncryptOptions options) {
        try {
            return new String(decrypt(cipher, options), YopConstants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new YopClientException("error happened when decrypt data", e);
        }
    }
}
