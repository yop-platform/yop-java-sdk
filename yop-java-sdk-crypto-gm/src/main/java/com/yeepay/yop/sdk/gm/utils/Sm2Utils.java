/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.utils;

import com.google.common.base.Charsets;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import com.yeepay.yop.sdk.utils.Encodes;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2020/12/26 2:03 下午
 */
public class Sm2Utils {
    private static X9ECParameters x9ECParameters = GMNamedCurves.getByName("sm2p256v1");
    private static ECParameterSpec ecParameterSpec = new ECParameterSpec(x9ECParameters.getCurve(), x9ECParameters.getG(), x9ECParameters.getN());
    public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
    public final static BigInteger SM2_ECC_N = CURVE.getOrder();
    public final static BigInteger SM2_ECC_H = CURVE.getCofactor();
    public final static BigInteger SM2_ECC_GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    public final static BigInteger SM2_ECC_GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
    public static final ECPoint G_POINT = CURVE.createPoint(SM2_ECC_GX, SM2_ECC_GY);
    public static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT,
            SM2_ECC_N, SM2_ECC_H);
    public static final int CURVE_LEN = getCurveLength(DOMAIN_PARAMS);

    static {
        SmUtils.init();
    }

    /**
     * 生成密钥对
     *
     * @return
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            kpGen.initialize(ecParameterSpec, new SecureRandom());
            KeyPair kp = kpGen.generateKeyPair();
            return kp;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * string类型的公钥转换成公钥对象
     *
     * @param pubKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     */
    public static PublicKey string2PublicKey(String pubKey) {
        try {
            byte[] x509Bytes = Encodes.decodeBase64(pubKey);
            X509EncodedKeySpec eks = new X509EncodedKeySpec(x509Bytes);
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePublic(eks);
        } catch (Exception e) {
            throw new YopClientException(e.getMessage());
        }
    }

    /**
     * string类型的私钥转换后私钥对象
     *
     * @param priKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     */
    public static PrivateKey string2PrivateKey(String priKey) {
        try {
            byte[] pkcs8Key = Encodes.decodeBase64(priKey);
            PKCS8EncodedKeySpec peks = new PKCS8EncodedKeySpec(pkcs8Key);
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePrivate(peks);
        } catch (Exception e) {
            throw new YopClientException(e.getMessage());
        }

    }

    /**
     * 密钥对象转换成string
     *
     * @param key
     * @return
     */
    public static String key2String(Key key) {
        return Encodes.encodeBase64(key.getEncoded());
    }

    /**
     * sm2密钥进行签名
     *
     * @param data
     * @param priKey
     * @return UR安全的base64编码后的签名
     */
    public static String sign(String data, BCECPrivateKey priKey) {
        return sign(data, priKey, null);
    }

    /**
     * sm2密钥进行签名
     *
     * @param data
     * @param priKey
     * @param options
     * @return base64编码后的签名
     */
    public static String sign(String data, BCECPrivateKey priKey, SignOptions options) {
        try {
            byte[] dataByte = data.getBytes(Charsets.UTF_8);
            if (null != options && !options.isUrlSafe()) {
                return Encodes.encodeBase64(sign(priKey, dataByte));
            }
            return Encodes.encodeUrlSafeBase64(sign(priKey, dataByte));
        } catch (CryptoException e) {
            throw new YopClientException("UnExpectedException occurred when sign content");
        }

    }

    /**
     * sm2密钥进行签名验证
     *
     * @param data
     * @param signature
     * @param publicKey
     * @return
     */
    public static boolean verifySign(String data, String signature, BCECPublicKey publicKey) {
        try {
            byte[] signByte = Encodes.decodeBase64(signature);
            byte[] dataByte = data.getBytes(Charsets.UTF_8);
            return verify(publicKey, dataByte, encodeSM2SignToDER(signByte));
        } catch (IOException e) {
            throw new YopClientException("IOException occurred when verify sign");
        }
    }

    /**
     * 把64字节的纯R+S字节数组编码成DER编码
     *
     * @param rawSign 64字节数组形式的SM2签名值，前32字节为R，后32字节为S
     * @return DER编码后的SM2签名值
     * @throws IOException
     */
    public static byte[] encodeSM2SignToDER(byte[] rawSign) throws IOException {
        //要保证大数是正数
        BigInteger r = new BigInteger(1, extractBytes(rawSign, 0, 32));
        BigInteger s = new BigInteger(1, extractBytes(rawSign, 32, 32));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(r));
        v.add(new ASN1Integer(s));
        return new DERSequence(v).getEncoded(ASN1Encoding.DER);
    }

    private static byte[] extractBytes(byte[] src, int offset, int length) {
        byte[] result = new byte[length];
        System.arraycopy(src, offset, result, 0, result.length);
        return result;
    }

    /**
     * 签名
     *
     * @param priKey  私钥
     * @param srcData 原文
     * @return 64字节的纯R+S字节流
     * @throws CryptoException
     */
    public static byte[] sign(BCECPrivateKey priKey, byte[] srcData) throws CryptoException {
        ECParameterSpec parameterSpec = priKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        ECPrivateKeyParameters priKeyParameters = new ECPrivateKeyParameters(priKey.getD(), domainParameters);
        //der编码后的签名值
        byte[] derSign = sign(priKeyParameters, null, srcData);

        //der解码过程
        ASN1Sequence as = DERSequence.getInstance(derSign);
        byte[] rBytes = ((ASN1Integer) as.getObjectAt(0)).getValue().toByteArray();
        byte[] sBytes = ((ASN1Integer) as.getObjectAt(1)).getValue().toByteArray();
        //由于大数的补0规则，所以可能会出现33个字节的情况，要修正回32个字节
        rBytes = fixToCurveLengthBytes(rBytes);
        sBytes = fixToCurveLengthBytes(sBytes);
        byte[] rawSign = new byte[rBytes.length + sBytes.length];
        System.arraycopy(rBytes, 0, rawSign, 0, rBytes.length);
        System.arraycopy(sBytes, 0, rawSign, rBytes.length, sBytes.length);
        return rawSign;
    }

    /**
     * 签名
     *
     * @param priKeyParameters 私钥
     * @param withId           可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @param srcData          源数据
     * @return DER编码后的签名值
     * @throws CryptoException
     */
    public static byte[] sign(ECPrivateKeyParameters priKeyParameters, byte[] withId, byte[] srcData)
            throws CryptoException {
        SM2Signer signer = new SM2Signer();
        CipherParameters param;
        ParametersWithRandom pwr = new ParametersWithRandom(priKeyParameters, new SecureRandom());
        if (withId != null) {
            param = new ParametersWithID(pwr, withId);
        } else {
            param = pwr;
        }
        signer.init(true, param);
        signer.update(srcData, 0, srcData.length);
        return signer.generateSignature();
    }

    /**
     * 验签
     *
     * @param pubKey  公钥
     * @param srcData 原文
     * @param sign    DER编码的签名值
     * @return
     */
    public static boolean verify(BCECPublicKey pubKey, byte[] srcData, byte[] sign) {
        ECParameterSpec parameterSpec = pubKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        ECPublicKeyParameters pubKeyParameters = new ECPublicKeyParameters(pubKey.getQ(), domainParameters);
        return verify(pubKeyParameters, null, srcData, sign);
    }

    /**
     * 验签
     *
     * @param pubKeyParameters 公钥
     * @param withId           可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @param srcData          原文
     * @param sign             DER编码的签名值
     * @return 验签成功返回true，失败返回false
     */
    public static boolean verify(ECPublicKeyParameters pubKeyParameters, byte[] withId, byte[] srcData, byte[] sign) {
        SM2Signer signer = new SM2Signer();
        CipherParameters param;
        if (withId != null) {
            param = new ParametersWithID(pubKeyParameters, withId);
        } else {
            param = pubKeyParameters;
        }
        signer.init(false, param);
        signer.update(srcData, 0, srcData.length);
        return signer.verifySignature(sign);
    }

    /**
     * @param pubKey  公钥
     * @param srcData 原文
     * @return 默认输出C1C3C2顺序的密文。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
     * @throws InvalidCipherTextException
     */
    public static byte[] encrypt(BCECPublicKey pubKey, byte[] srcData) throws InvalidCipherTextException {
        ECParameterSpec parameterSpec = pubKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        ECPublicKeyParameters pubKeyParameters = new ECPublicKeyParameters(pubKey.getQ(), domainParameters);
        return encrypt(SM2Engine.Mode.C1C3C2, pubKeyParameters, srcData);
    }

    /**
     * @param mode             指定密文结构，旧标准的为C1C2C3，新的[《SM2密码算法使用规范》 GM/T 0009-2012]标准为C1C3C2
     * @param pubKeyParameters 公钥
     * @param srcData          原文
     * @return 根据mode不同，输出的密文C1C2C3排列顺序不同。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
     * @throws InvalidCipherTextException
     */
    public static byte[] encrypt(SM2Engine.Mode mode, ECPublicKeyParameters pubKeyParameters, byte[] srcData)
            throws InvalidCipherTextException {
        SM2Engine engine = new SM2Engine(mode);
        ParametersWithRandom pwr = new ParametersWithRandom(pubKeyParameters, new SecureRandom());
        engine.init(true, pwr);
        return engine.processBlock(srcData, 0, srcData.length);
    }

    /**
     * @param priKey    私钥
     * @param sm2Cipher 默认输入C1C3C2顺序的密文。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
     * @return 原文。SM2解密返回了数据则一定是原文，因为SM2自带校验，如果密文被篡改或者密钥对不上，都是会直接报异常的。
     * @throws InvalidCipherTextException
     */
    public static byte[] decrypt(BCECPrivateKey priKey, byte[] sm2Cipher) throws InvalidCipherTextException {
        ECParameterSpec parameterSpec = priKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        ECPrivateKeyParameters priKeyParameters = new ECPrivateKeyParameters(priKey.getD(), domainParameters);
        return decrypt(SM2Engine.Mode.C1C3C2, priKeyParameters, sm2Cipher);
    }

    /**
     * @param mode             指定密文结构，旧标准的为C1C2C3，新的[《SM2密码算法使用规范》 GM/T 0009-2012]标准为C1C3C2
     * @param priKeyParameters 私钥
     * @param sm2Cipher        根据mode不同，需要输入的密文C1C2C3排列顺序不同。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
     * @return 原文。SM2解密返回了数据则一定是原文，因为SM2自带校验，如果密文被篡改或者密钥对不上，都是会直接报异常的。
     * @throws InvalidCipherTextException
     */
    public static byte[] decrypt(SM2Engine.Mode mode, ECPrivateKeyParameters priKeyParameters, byte[] sm2Cipher)
            throws InvalidCipherTextException {
        SM2Engine engine = new SM2Engine(mode);
        engine.init(false, priKeyParameters);
        return engine.processBlock(sm2Cipher, 0, sm2Cipher.length);
    }

    /**
     * 只获取私钥里的d值，32字节
     *
     * @param privateKey
     * @return
     */
    public static byte[] getRawPrivateKey(BCECPrivateKey privateKey) {
        return fixToCurveLengthBytes(privateKey.getD().toByteArray());
    }

    /**
     * 只获取公钥里的XY分量，64字节
     *
     * @param publicKey
     * @return 64字节数组
     */
    public static byte[] getRawPublicKey(BCECPublicKey publicKey) {
        byte[] src65 = publicKey.getQ().getEncoded(false);
        byte[] rawXY = new byte[CURVE_LEN * 2];
        System.arraycopy(src65, 1, rawXY, 0, rawXY.length);
        return rawXY;
    }

    private static byte[] fixToCurveLengthBytes(byte[] src) {
        if (src.length == CURVE_LEN) {
            return src;
        }

        byte[] result = new byte[CURVE_LEN];
        if (src.length > CURVE_LEN) {
            System.arraycopy(src, src.length - result.length, result, 0, result.length);
        } else {
            System.arraycopy(src, 0, result, result.length - src.length, src.length);
        }
        return result;
    }

    public static int getCurveLength(ECDomainParameters domainParams) {
        return (domainParams.getCurve().getFieldSize() + 7) / 8;
    }
}
