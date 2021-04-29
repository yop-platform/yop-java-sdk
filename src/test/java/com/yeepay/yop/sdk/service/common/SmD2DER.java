/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.utils.Encodes;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.FixedPointCombMultiplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/4/25 15:58
 */
@Ignore
public class SmD2DER {

    private String name = "sm2p256v1";
//    private String name = "secp256r1";

    @Before
    public void setUp() throws Exception {
        Security.removeProvider("SunEC");
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void test() throws NoSuchAlgorithmException, InvalidKeySpecException {
//        System.out.println(new BigInteger("66213661209421575346612329707679341349846984049698527221136155542979076962211", 10).toString(16));
//        System.out.println(new BigInteger("B9C9A6E04E9C91F7BA880429273747D7EF5DDEB0BB2FF6317EB00BEF331A83081A6994B8993F3F5D6EADDDB81872266C87C018FB4162F5AF347B483E24620207", 16).toString(10));

//        PrivateKey privateKey = gen("00B9AB0B828FF68872F21A837FC303668428DEA11DCD1B24429D0C99E24EED83D5", 16);
//        PrivateKey privateKey = gen("19791495634805870336639420566697069981441064876895811215840904284068948694620", 10);// QA 平台私钥
        PrivateKey privateKey = gen("92639aaa2cd9aa7f1dffea57afc5091ae747081934e7cb8f793e4fab4b7037a3", 16);// 测试商户
//        PrivateKey privateKey = gen("66213661209421575346612329707679341349846984049698527221136155542979076962211", 10);// QA 测试商户
        PKCS8EncodedKeySpec spec8 = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        System.out.println(Encodes.encodeBase64(spec8.getEncoded()));

        // 计算公钥
        ECParameterSpec spec = ECNamedCurveTable.getParameterSpec(name);
        ECPoint q = new FixedPointCombMultiplier().multiply(spec.getG(), ((BCECPrivateKey) privateKey).getD()).normalize();

//        49490cb687c07c62248fa043d4adb461c151db8cea9abb9bec86952681ec7f1d
//        e5a024d9bca7b15cfa569d863f65bfa63ebdf089eb8b0876ca0741e88a64d30b
        String publicKeyStr = q.getAffineXCoord().toString() + q.getAffineYCoord().toString();
        System.out.println(publicKeyStr);

        //
        ECPublicKeySpec keySpec = new ECPublicKeySpec(q, spec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        spec8 = new PKCS8EncodedKeySpec(publicKey.getEncoded());
        System.out.println(Encodes.encodeBase64(spec8.getEncoded()));
    }

    private PrivateKey gen(String num, int jinzhi) {
        ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec(name);
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(num, jinzhi), ecParameterSpec);
        System.out.println("16进制："+new BigInteger(num, jinzhi).toString(16));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return keyFactory.generatePrivate(privateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

}
