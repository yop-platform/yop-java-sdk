/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.security.encrypt;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorAdaptor;
import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

/**
 * title: 非对称加解密器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public class YopSm2Encryptor extends YopEncryptorAdaptor {

    private static final String ENCRYPT_ALG = "SM2";

    static {
        SmUtils.init();
    }

    // mode 指定密文结构，旧标准的为C1C2C3，新的[《SM2密码算法使用规范》 GM/T 0009-2012]标准为C1C3C2
    // 我们采用C1C3C2
    // 根据mode不同，输出的密文C1C2C3排列顺序不同。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
    private static final ThreadLocal<SM2Engine> engineThreadLocal = new ThreadLocal<SM2Engine>() {
        @Override
        protected SM2Engine initialValue() {
            return new SM2Engine(SM2Engine.Mode.C1C3C2);
        }
    };

    @Override
    public List<String> supportedAlgs() {
        return Collections.singletonList(ENCRYPT_ALG);
    }

    @Override
    public EncryptOptions doInitEncryptOptions(String encryptAlg) throws Exception {
        // 暂无需求
        return null;
    }

    @Override
    public byte[] encrypt(byte[] plain, EncryptOptions options) {
        try {
            SM2Engine engine = engineThreadLocal.get();
            ECPublicKeyParameters pubKeyParameters = convertPublicKeyToParameters(
                    (BCECPublicKey) ((PKICredentialsItem) ((YopPlatformCredentials)
                            options.getCredentials()).getCredential()).getPublicKey());
            ParametersWithRandom pwr = new ParametersWithRandom(pubKeyParameters, new SecureRandom());
            engine.init(true, pwr);
            return engine.processBlock(plain, 0, plain.length);
        } catch (Throwable e) {
            throw new YopClientException("error happened when encrypt with SM2 alg", e);
        }
    }

    @Override
    public InputStream encrypt(InputStream plain, EncryptOptions options) {
        // 暂无需求
        return null;
    }

    @Override
    public byte[] decrypt(byte[] cipher, EncryptOptions options) {
        try {
            SM2Engine engine = engineThreadLocal.get();
            ECPrivateKeyParameters priKeyParameters = convertPrivateKeyToParameters((BCECPrivateKey)
                    ((YopPKICredentials) options.getCredentials()).getCredential().getPrivateKey());
            engine.init(false, priKeyParameters);
            return engine.processBlock(cipher, 0, cipher.length);
        } catch (Throwable e) {
            throw new YopClientException("error happened when decrypt with SM2 alg", e);
        }
    }

    @Override
    public InputStream decrypt(InputStream cipher, EncryptOptions options) {
        // 暂无需求
        return null;
    }

    private ECPublicKeyParameters convertPublicKeyToParameters(BCECPublicKey ecPubKey) {
        ECParameterSpec parameterSpec = ecPubKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        return new ECPublicKeyParameters(ecPubKey.getQ(), domainParameters);
    }

    private ECPrivateKeyParameters convertPrivateKeyToParameters(BCECPrivateKey ecPriKey) {
        ECParameterSpec parameterSpec = ecPriKey.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        return new ECPrivateKeyParameters(ecPriKey.getD(), domainParameters);
    }
}
