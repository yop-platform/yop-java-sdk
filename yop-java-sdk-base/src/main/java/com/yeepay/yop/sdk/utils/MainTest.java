/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/21 9:02 下午
 */
public class MainTest {
    public static void main(String[] args) {
        String privKeyPem = "MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIO7CuS6SLoPvIFV/7onGDZLz8Z7QHokp4N83PcarImbWoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAARgmS9r0r4mSaajawAYbgwVcgX3wDs/ed3FY6NCEk+9Is4wWNAlKvr9Zkg+MN9VxIekX/MCZv6EgrhPg7qXo3Zy";
        String pubKeyPem = "MIIBMzCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBA0IABGCZL2vSviZJpqNrABhuDBVyBffAOz953cVjo0IST70izjBY0CUq+v1mSD4w31XEh6Rf8wJm/oSCuE+DupejdnI=";
        PrivateKey privateKey = Sm2Utils.string2PrivateKey(privKeyPem);
        PublicKey publicKey = Sm2Utils.string2PublicKey(pubKeyPem);
        String plaintText = "a=123";
        String sign = Sm2Utils.sign(plaintText, (BCECPrivateKey) privateKey);
        System.out.println(sign);
        System.out.println(Sm2Utils.verifySign("plaintText", sign, (BCECPublicKey) publicKey));

    }
}
