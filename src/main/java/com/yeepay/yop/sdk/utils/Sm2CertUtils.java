/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.exception.YopServiceException;
import org.bouncycastle.asn1.pkcs.ContentInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12SafeBag;
import org.bouncycastle.pkcs.PKCS12SafeBagFactory;
import org.bouncycastle.pkcs.jcajce.JcePKCSPBEInputDecryptorProviderBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.*;
import java.util.Date;
import java.util.List;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/2/3 4:58 下午
 */
public class Sm2CertUtils {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static String getPrivateFormSm2(String pemPri, String pwd) {
        try {
            SM2PrivateKey privateKeyFromSM2 = KeyUtil.getPrivateKeyFromSM2(pemPri.getBytes(), pwd);
            BCECPrivateKey bcecPrivateKey = new BCECPrivateKey(privateKeyFromSM2.getAlgorithm(),
                    new ECPrivateKeyParameters(privateKeyFromSM2.getD(), null),
                    privateKeyFromSM2.getParams());
            return Encodes.encodeBase64(bcecPrivateKey.getEncoded());
        } catch (PKIException e) {
            throw new YopServiceException("illegal cert", e);
        }

    }

    public static BCECPublicKey getBCECPublicKey(X509Certificate sm2Cert) {
        ECPublicKey pubKey = (ECPublicKey) sm2Cert.getPublicKey();
        ECPoint q = pubKey.getQ();
        ECParameterSpec parameterSpec = new ECParameterSpec(Sm2Utils.CURVE, Sm2Utils.G_POINT,
                Sm2Utils.SM2_ECC_N, Sm2Utils.SM2_ECC_H);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(q, parameterSpec);
        return new BCECPublicKey(pubKey.getAlgorithm(), pubKeySpec,
                BouncyCastleProvider.CONFIGURATION);
    }

    /**
     * 校验证书签名
     *
     * @param issuerPubKey 从颁发者CA证书中提取出来的公钥
     * @param cert         待校验的证书
     */
    public static void verifyCertificate(BCECPublicKey issuerPubKey, X509Certificate cert) throws NoSuchProviderException, CertificateException,
            NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        checkCertDate(cert);
        if (null != issuerPubKey) {
            cert.verify(issuerPubKey, BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    /**
     * 校验证书有效期（过期后24小时内继续可用）
     * @param certificate
     * @throws CertificateExpiredException
     * @throws CertificateNotYetValidException
     */
    private static void checkCertDate(X509Certificate certificate) throws CertificateExpiredException, CertificateNotYetValidException {
        Date now = new Date();
        long time24HoursAgo = now.getTime() - 24 * 3600 * 1000;
        if (time24HoursAgo > certificate.getNotAfter().getTime()) {
            throw new CertificateExpiredException("certificate expired on " + certificate.getNotAfter().getTime());
        }

        if (now.getTime() < certificate.getNotBefore().getTime()) {
            throw new CertificateNotYetValidException("certificate not valid till " + certificate.getNotBefore().getTime());
        }
    }

    public static X509Certificate getX509Certificate(String certFilePath) throws IOException, CertificateException,
            NoSuchProviderException {
        InputStream is = null;
        try {
            is = new FileInputStream(certFilePath);
            return getX509Certificate(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static X509Certificate getX509Certificate(byte[] certBytes) throws CertificateException,
            NoSuchProviderException {
        ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
        return getX509Certificate(bais);
    }

    public static X509Certificate getX509Certificate(InputStream is) throws CertificateException,
            NoSuchProviderException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
        return (X509Certificate) cf.generateCertificate(is);
    }

    public static CertPath getCertificateChain(String certChainPath) throws IOException, CertificateException,
            NoSuchProviderException {
        InputStream is = null;
        try {
            is = new FileInputStream(certChainPath);
            return getCertificateChain(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public static CertPath getCertificateChain(byte[] certChainBytes) throws CertificateException,
            NoSuchProviderException {
        ByteArrayInputStream bais = new ByteArrayInputStream(certChainBytes);
        return getCertificateChain(bais);
    }

    public static byte[] getCertificateChainBytes(CertPath certChain) throws CertificateEncodingException {
        return certChain.getEncoded("PKCS7");
    }

    public static CertPath getCertificateChain(InputStream is) throws CertificateException, NoSuchProviderException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
        return cf.generateCertPath(is, "PKCS7");
    }

    public static CertPath getCertificateChain(List<X509Certificate> certs) throws CertificateException,
            NoSuchProviderException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
        return cf.generateCertPath(certs);
    }

    public static X509Certificate getX509CertificateFromPfx(byte[] pfxDER, String passwd) throws Exception {
        InputDecryptorProvider inputDecryptorProvider = new JcePKCSPBEInputDecryptorProviderBuilder()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(passwd.toCharArray());
        PKCS12PfxPdu pfx = new PKCS12PfxPdu(pfxDER);

        ContentInfo[] infos = pfx.getContentInfos();
        if (infos.length != 2) {
            throw new Exception("Only support one pair ContentInfo");
        }

        for (int i = 0; i != infos.length; i++) {
            if (infos[i].getContentType().equals(PKCSObjectIdentifiers.encryptedData)) {
                PKCS12SafeBagFactory dataFact = new PKCS12SafeBagFactory(infos[i], inputDecryptorProvider);
                PKCS12SafeBag[] bags = dataFact.getSafeBags();
                X509CertificateHolder certHoler = (X509CertificateHolder) bags[0].getBagValue();
                return Sm2CertUtils.getX509Certificate(certHoler.getEncoded());
            }
        }

        throw new Exception("Not found X509Certificate in this pfx");
    }

    public static BCECPublicKey getPublicKeyFromPfx(byte[] pfxDER, String passwd) throws Exception {
        return Sm2CertUtils.getBCECPublicKey(getX509CertificateFromPfx(pfxDER, passwd));
    }

}

