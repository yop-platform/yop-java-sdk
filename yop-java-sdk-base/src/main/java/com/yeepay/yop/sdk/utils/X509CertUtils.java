/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.crypto.X509CertSupportFactory;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * title: x509证书工具类<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/2/3 4:58 下午
 */
public class X509CertUtils {

    /**
     * 校验证书签名
     *
     * @param certType     证书类型
     * @param issuerPubKey 从颁发者CA证书中提取出来的公钥
     * @param cert         待校验的证书
     */
    public static void verifyCertificate(CertTypeEnum certType, PublicKey issuerPubKey, X509Certificate cert) throws NoSuchProviderException, CertificateException,
            NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        checkCertDate(cert);
        if (null != issuerPubKey) {
            X509CertSupportFactory.getSupport(certType.getValue()).verifyCertificate(issuerPubKey, cert);
        }
    }

    /**
     * 校验证书有效期（过期后24小时内继续可用，过期前72小时需刷新）
     *
     * @param certificate 证书
     * @throws CertificateExpiredException
     * @throws CertificateNotYetValidException
     * @return true: 需要刷新，false：不需要
     */
    public static boolean checkCertDate(X509Certificate certificate) throws CertificateExpiredException, CertificateNotYetValidException {
        // TODO 商户可配置
        Date now = new Date();
        long time24HoursAgo = now.getTime() - 24 * 3600 * 1000;
        if (time24HoursAgo > certificate.getNotAfter().getTime()) {
            throw new CertificateExpiredException("certificate expired on " + certificate.getNotAfter().getTime());
        }

        if (now.getTime() < certificate.getNotBefore().getTime()) {
            throw new CertificateNotYetValidException("certificate not valid till " + certificate.getNotBefore().getTime());
        }
        // 72小时内过期，需刷新
        long time24HoursAfter = now.getTime() + 72 * 3600 * 1000;
        return time24HoursAfter > certificate.getNotAfter().getTime() ;
    }

    public static X509Certificate getX509Certificate(CertTypeEnum certType, byte[] certBytes) throws CertificateException,
            NoSuchProviderException {
        ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
        return getX509Certificate(certType, bais);
    }

    public static X509Certificate getX509Certificate(CertTypeEnum certType, InputStream is) throws CertificateException,
            NoSuchProviderException {
        return X509CertSupportFactory.getSupport(certType.getValue()).generate(is);
    }
}

