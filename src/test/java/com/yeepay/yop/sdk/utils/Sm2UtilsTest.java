package com.yeepay.yop.sdk.utils;

import org.junit.Ignore;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/4/27 21:11
 */
@Ignore
public class Sm2UtilsTest {

    @Test
    public void priKey() {
        PrivateKey priKey = Sm2Utils.string2PrivateKey("MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIJJjmqos2ap/Hf/qV6/FCRrnRwgZNOfLj3k+T6tLcDejoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAASlKx2pDQcXfgdCZb8E8GZWUpIHkEBgkRnx+5tueXwcaMJ1om0qv1bxjxLUyHiVG3GOC0Qr1m2+rU+2lVTWYwP4");
        priKey.getEncoded();
    }

    @Test
    public void pubKey() {
        PublicKey pubKey = Sm2Utils.string2PublicKey("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEh8wDeJDYKLhlbU/SMXA3+dqQZydLiyrHuBfZ/hKVqqPiVrxuA20kneJd4bF5EpZCPivJHcgAm8F8K+GqjGe73w==");
        pubKey.getEncoded();
    }

}