/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.crypto;

import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.SmInitUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * title: sm2证书支持<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class X509Sm2CertSupport implements X509CertSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(X509Sm2CertSupport.class);

    static {
        SmInitUtils.init();
    }

    @Override
    public X509Certificate generate(InputStream inputStream) throws CertificateException, NoSuchProviderException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
        return (X509Certificate) cf.generateCertificate(inputStream);
    }

    @Override
    public void verifyCertificate(PublicKey issuerPubKey, X509Certificate cert) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        cert.verify(issuerPubKey, BouncyCastleProvider.PROVIDER_NAME);
    }

    @Override
    public String support() {
        return CertTypeEnum.SM2.getValue();
    }
}
