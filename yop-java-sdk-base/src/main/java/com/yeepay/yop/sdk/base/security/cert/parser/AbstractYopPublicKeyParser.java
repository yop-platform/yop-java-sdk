/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.cert.parser;

import com.yeepay.yop.sdk.base.security.cert.X509CertSupportFactory;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;

import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
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
    protected X509Certificate getX509Cert(String certPath, CertTypeEnum certType) throws CertificateException, NoSuchProviderException {
        InputStream certStream = null;
        try {
            certStream = FileUtils.getResourceAsStream(certPath);
            return X509CertSupportFactory.getSupport(certType.getValue()).generate(certStream);
        } finally {
            StreamUtils.closeQuietly(certStream);
        }
    }
}
