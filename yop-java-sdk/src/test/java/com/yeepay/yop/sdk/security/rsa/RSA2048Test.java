package com.yeepay.yop.sdk.security.rsa;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.utils.Encodes;
import org.junit.Test;

import java.security.PublicKey;

import static org.junit.Assert.assertTrue;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/4/27 11:28
 */
public class RSA2048Test {

    @Test
    public void name() {
        String pubKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6p0XWjscY+gsyqKRhw9MeLsEmhFdBRhT2emOck/F1Omw38ZWhJxh9kDfs5HzFJMrVozgU+SJFDONxs8UB0wMILKRmqfLcfClG9MyCNuJkkfm0HFQv1hRGdOvZPXj3Bckuwa7FrEXBRYUhK7vJ40afumspthmse6bs6mZxNn/mALZ2X07uznOrrc2rk41Y2HftduxZw6T4EmtWuN2x4CZ8gwSyPAW5ZzZJLQ6tZDojBK4GZTAGhnn3bg5bBsBlw2+FLkCQBuDsJVsFPiGh/b6K/+zGTvWyUcu+LUj2MejYQELDO3i2vQXVDk7lVi2/TcUYefvIcssnzsfCfjaorxsuwIDAQAB";
        PublicKey publicKey = RSAKeyUtils.string2PublicKey(pubKeyStr);

        String sourceData = "{\"requestId\":\"4e10a241-357a-482f-ac50-3a5f95d976f0\",\"code\":\"40047\",\"message\":\"\\u9274\\u6743\\u8ba4\\u8bc1\\u5931\\u8d25\",\"subCode\":\"isv.authentication.digest.verify-failure\",\"subMessage\":\"\\u8bf7\\u6c42\\u9a8c\\u7b7e\\u5931\\u8d25\",\"docUrl\":\"https://open.yeepay.com/docs/v2/platform/sdk_guide/error_code/index.html#platform_isv_authentication_digest_verify-failure\"}";
        String signToBase64 = "wvcwYtqBexLi5Dt+OPyLFOjqkRaV3zJf1dl7Q558ja5ZKCTONc/yqwbDXosLmBuuprIKQ5bptIVaeP0xXh9/x2LeJOMLeFL0HGLJH+hzlajf4rer+RXbkZGOPYpyoFsUKAOtGMic0FuhSvLh1+kaLgJKisMUOkAri9Yp9wsxYLlfQXfYPg6E3WDCu0R90kqYGRQuSu0PxKsdCt0HeIvam7cZ/3kLUDBQdHAKcbgdJPPyv0Q2Tfi/A71B7RzrJ2fvtcH3fVzWXE/sPyeSl1wl7iszgYyw3AnywlNbdjPEH5U0oLZGa58uvR4CoeND6vcBjFBeuPjl7c1GLXq1MwLYjg==";

        RSA2048 rsa2048 = new RSA2048();
        boolean verifySign = rsa2048.verifySign(publicKey, sourceData.getBytes(YopConstants.DEFAULT_CHARSET), Encodes.decodeBase64(signToBase64));

        assertTrue(verifySign);
    }
}