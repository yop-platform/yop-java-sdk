/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.crypto;

import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;

import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * title: Yop公钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public abstract class AbstractYopPublicKeyParser {

    /**
     * 解析x509证书
     *
     * @param certPath 文件路径
     * @return X509Certificate
     * @throws CertificateException
     * @throws NoSuchProviderException
     */
    protected X509Certificate getX509Cert(String certPath) throws CertificateException, NoSuchProviderException {
        InputStream certStream = null;
        try {
            certStream = FileUtils.getResourceAsStream(certPath);
            CertificateFactory cf = getCertificateFactory();
            return (X509Certificate) cf.generateCertificate(certStream);
        } finally {
            StreamUtils.closeQuietly(certStream);
        }
    }

    protected abstract CertificateFactory getCertificateFactory() throws CertificateException, NoSuchProviderException;
}
