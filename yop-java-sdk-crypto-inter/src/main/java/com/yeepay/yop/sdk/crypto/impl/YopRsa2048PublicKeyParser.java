/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.crypto.impl;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.crypto.AbstractYopPublicKeyParser;
import com.yeepay.yop.sdk.crypto.YopCertCategory;
import com.yeepay.yop.sdk.crypto.YopCertParser;
import com.yeepay.yop.sdk.crypto.YopPublicKey;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.CharacterConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * title: Rsa公钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopRsa2048PublicKeyParser extends AbstractYopPublicKeyParser implements YopCertParser {

    @Override
    public YopPublicKey parse(YopCertConfig certConfig) {
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("Can't init YOP public key!,  Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    return new YopPublicKey(RSAKeyUtils.string2PublicKey(certConfig.getValue()));
                } catch (Exception ex) {
                    throw new YopClientException("Can't init YOP public key!, cert_config:" + certConfig, ex);
                }
            case FILE_CER:
                try {
                    return new YopPublicKey(getX509Cert(certConfig.getValue(), CertTypeEnum.RSA2048));
                } catch (Exception e) {
                    throw new YopClientException("Can't init YOP public key! Cer file is error.", e);
                }
            default:
                throw new YopClientException("Can't init YOP public key!, cert_config:" + certConfig);
        }
    }

    @Override
    public String parserId() {
        return StringUtils.joinWith(CharacterConstants.COMMA, YopCertCategory.PUBLIC, CertTypeEnum.RSA2048);
    }
}