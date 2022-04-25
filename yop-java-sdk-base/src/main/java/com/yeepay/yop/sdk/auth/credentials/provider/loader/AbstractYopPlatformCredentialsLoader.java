/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider.loader;

import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.EnvUtils;
import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.Sm2CertUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * title: Yop平台凭证加载抽象类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/25
 */
public abstract class AbstractYopPlatformCredentialsLoader implements YopPlatformCredentialsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractYopPlatformCredentialsLoader.class);

    protected static X509Certificate cfcaRoot, yopInter;
    protected static String defaultCertPath = "config/certs", defaultCfcaRootFile = "cfca_root.pem",
            defaultYopInterFile = "yop_inter.pem";
    protected static YopCertStore defaultYopCertStore;

    static {
        try {
            if (!EnvUtils.isProd()) {
                String env = EnvUtils.currentEnv(),
                        envPrefix = StringUtils.substringBefore(env, "_");
                defaultCfcaRootFile = envPrefix + "_" + defaultCfcaRootFile;
                defaultYopInterFile = envPrefix + "_" + defaultYopInterFile;
            }
            defaultYopCertStore = new YopCertStore(defaultCertPath);

            cfcaRoot = getX508Cert(defaultCertPath + "/" + defaultCfcaRootFile);
            verifyCertChain("cfca root", null, cfcaRoot);

            yopInter = getX508Cert(defaultCertPath + "/" + defaultYopInterFile);
            verifyCertChain("yop inter", (BCECPublicKey) cfcaRoot.getPublicKey(), yopInter);
        } catch (Exception e) {
            LOGGER.error("error when load parent certs, ex:", e);
        }
    }

    protected static void verifyCertChain(String certName, BCECPublicKey issuer, X509Certificate cert) {
        try {
            Sm2CertUtils.verifyCertificate(issuer, cert);
        } catch (Exception e) {
            throw new YopClientException("invalid " + certName + " cert, detail:" + e.getMessage());
        }
    }

    private static X509Certificate getX508Cert(String certPath) throws CertificateException, NoSuchProviderException {
        InputStream certStream = null;
        try {
            certStream = FileUtils.getResourceAsStream(certPath);
            return Sm2CertUtils.getX509Certificate(certStream);
        } finally {
            StreamUtils.closeQuietly(certStream);
        }
    }
}
