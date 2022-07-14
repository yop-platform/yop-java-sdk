package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import com.yeepay.yop.sdk.gm.utils.Sm2Utils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.junit.Ignore;
import org.junit.Test;

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
        BCECPrivateKey priKey = (BCECPrivateKey) Sm2Utils.string2PrivateKey("MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIJJjmqos2ap/Hf/qV6/FCRrnRwgZNOfLj3k+T6tLcDejoIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAASlKx2pDQcXfgdCZb8E8GZWUpIHkEBgkRnx+5tueXwcaMJ1om0qv1bxjxLUyHiVG3GOC0Qr1m2+rU+2lVTWYwP4");
        System.out.println(priKey.getEncoded());
    }

    @Test
    public void pubKey() {
        BCECPublicKey pubKey = (BCECPublicKey) SmUtils.string2PublicKey("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEd8OsYO4yIFNboF68nk1Yl9zquW/OuJSjGLz8Yu7ldV3ro9pGb5g079hWGeEZ+DqaHex3YzP7dVuQ9KV81pRa3w==");
        System.out.println(pubKey.getQ());
    }

}