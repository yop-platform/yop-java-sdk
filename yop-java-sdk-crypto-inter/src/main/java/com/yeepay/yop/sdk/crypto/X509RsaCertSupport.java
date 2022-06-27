/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.crypto;

import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * title: rsa证书支持<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class X509RsaCertSupport implements X509CertSupport {

    @Override
    public X509Certificate generate(InputStream inputStream) throws CertificateException, NoSuchProviderException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(inputStream);
    }

    @Override
    public void verifyCertificate(PublicKey issuerPubKey, X509Certificate cert) throws NoSuchProviderException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        cert.verify(issuerPubKey);
    }

    @Override
    public String support() {
        return CertTypeEnum.RSA2048.getValue();
    }

    @Override
    public void writeToFile(X509Certificate cert, File file) throws IOException, CertificateEncodingException {
        // TODO
    }
}
