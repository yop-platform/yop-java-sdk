/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.cert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * title: x509证书支持类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public interface X509CertSupport {

    /**
     * 生成x509证书
     *
     * @param inputStream 证书流
     * @return X509Certificate
     */
    X509Certificate generate(InputStream inputStream) throws CertificateException, NoSuchProviderException;


    /**
     * 校验证书签名
     *
     * @param issuerPubKey 从颁发者CA证书中提取出来的公钥
     * @param cert         待校验的证书
     */
    void verifyCertificate(PublicKey issuerPubKey, X509Certificate cert) throws NoSuchProviderException, CertificateException,
            NoSuchAlgorithmException, InvalidKeyException, SignatureException;

    /**
     * 密钥类型
     *
     * @return CertTypeEnum
     */
    String support();

    /**
     * 将证书写入文件
     *
     * @param cert 证书
     * @param file 文件路径
     */
    void writeToFile(X509Certificate cert, File file) throws IOException, CertificateEncodingException;
}
