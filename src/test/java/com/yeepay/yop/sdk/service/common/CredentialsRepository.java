/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.Sm2Utils;

import java.security.PrivateKey;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021-05-08
 */
public class CredentialsRepository {

    private static final Map<String, String> privateKeyStrMap = Maps.newHashMapWithExpectedSize(2);
    private static final Map<String, PrivateKey> privateKeyMap = Maps.newHashMapWithExpectedSize(2);
    private static final Map<String, CertTypeEnum> supportCertMap = Maps.newHashMapWithExpectedSize(2);
    private static final Map<String, String> securityMap = Maps.newHashMapWithExpectedSize(2);
    private static final Map<String, String> encryptKeyStrMap = Maps.newHashMapWithExpectedSize(2);

    static {
        privateKeyStrMap.put("yop-boss", "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCSx5/9gJYOaefpTIUGVSJkOobtZDIo0lKj0m62fEfTA7QgWsv2AJJiMGv4YdOecVrm8FxUQVL9p64mrs96WN70ovtSuaSWwoft7NUMTWvRsTYnJkaDLIpU8KKNbrSXZx7pb8T0Shv2ZS2XY0Mo3UQrXSbWCnp+8WkZRTtfMtsPsf9b3J//q4JAxtW7SOn5AFV9AdmQXy7Zh5rZg3B4oQKcJ/KHcPtid0OClq4ma9fWeNSsnrW0AXyO4Ng4H5ls8TCFzYO4PA9mq9zc+WIQtgXIQRc5ED+kcbnkTIIKl02lmcv9hsCT32rWIivhwRORTGvM+tsHPDHBmuwLiNsqpG8HAgMBAAECggEBAJBEU5WX8GVkZMRjKJCr1uzKtdnY1JBZAU7xw03r47CdAEuY0sYNk9OcolL03EnsQpugfi92MXsNd9eflGA9v46WLw4FV6eytmX9lP3Njv8A/igGr+G4QpLwHeWDfU8e1Tw+VkiCGu/YTLJypw0gRiOVIFna3MGuyE1FRfDxDG1kUl6RX188ACATUEnMEkBrhL4fFcqftqH41dkHJIgZ4/77oj1UczpSV6pImrPAXd9ypjs6v+6x7boxBQbW2Todbr6mlPgCzl36hziJyl8N20I9ol/deClX0ab1rlfbK0fqF4ZXC6BH3GKfxdmbE3UBMW4fOPdVHuQ0IN+tApn1w+kCgYEA7im6hHI2ocvt1IP3I6gqT3v9wV8/ZKBaQaTQFDOcj/93RpLgUmyKcc4p/QduUZ4mJHjgy6RioPThHkrQkC8ONPxPxaXJL1Xr9+nhqOMa0EBAlBTx7ltmJimZH53BTn2UsHc14WgkXLYnHFXAGnS2L+Xxvvu3l1NW5Ydito063SMCgYEAncXRDrOTbN2cUBaVD+meRbZZTmnYqoWs271vHNwbow71ab6r+gUat7PwS2lBqPScqna8SsvE4a6Aw8F9R6xN1I2z22pZAGOBYQA8Q0V1bcg41SW3eOi95I/hjESF9YrWcsTrcAi/YWYlJVwkd3jTfbTeyaIW2qAK+Z7gS3qf3s0CgYEAtnI3FVFdcrMDfaKeh61CxGMq8KDjslV47wKv+FnVXSaKHAFWYS4PHMGfvtubcmDhvVzwcHB8sesGLauIfHvfuU10Wuf26BE9VEzR9wwLNW+TSR2GfF9+MEv7ppG2TUe8yTZ5izS5bmCIM5epM1snWigf+ntgmEdasTj2sPweFNsCgYA1h9SyxEMVAOv0UHUq/PzycjhC3q7gzJIlzFRS2muWG5Ew27zGC81Q3wB81a2tgbFWNQsV5aVbXTXbNV8oXlHZ+Go53A9ujlRrcQUBXiPFp9WAnFdv8qfbUOYaDXXWJdE1B5NKY+1rQpj/4A+PabN4R1H/37sZWovevgUkFur/UQKBgEK2LxiX5vNgZmcnz5+N2ISqXGBrjq7CMWYIh79PS3dg6ru2mHRKhtYlRAHfQL/z0R3ImlScRbAH0rIweZeg0nHq2qEWGIkkqfX49tySgSIMOxLeoq3mLtiB0xP5doACd62DVhYb8yzTYli7//NWT4AbykQZrnFwKoahhVMwgpON");
        privateKeyMap.put("yop-boss", RSAKeyUtils.string2PrivateKey(privateKeyStrMap.get("yop-boss")));
        supportCertMap.put("yop-boss", CertTypeEnum.RSA2048);
        securityMap.put("yop-boss", "YOP-RSA2048-SHA256");
        privateKeyStrMap.put("app_100800095600032", "MIICSwIBADCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA/////v////////////////////8AAAAA//////////8wRAQg/////v////////////////////8AAAAA//////////wEICjp+p6dn140TVqeS89lCafzl4n1FauPkt28vUFNlA6TBEEEMsSuLB8ZgRlfmQRGajnJlI/jC7/yZgvhcVpFiTNMdMe8Nzai9PZ3nFm9zuNraSFT0KmHfMYqR0AC3zLlITnwoAIhAP////7///////////////9yA99rIcYFK1O79Ak51UEjAgEBBIIBVTCCAVECAQEEIO74yo+DcjHd6ekJWNsWDp+Eq3reC64S77sjKOq+nZQ4oIHjMIHgAgEBMCwGByqGSM49AQECIQD////+/////////////////////wAAAAD//////////zBEBCD////+/////////////////////wAAAAD//////////AQgKOn6np2fXjRNWp5Lz2UJp/OXifUVq4+S3by9QU2UDpMEQQQyxK4sHxmBGV+ZBEZqOcmUj+MLv/JmC+FxWkWJM0x0x7w3NqL09necWb3O42tpIVPQqYd8xipHQALfMuUhOfCgAiEA/////v///////////////3ID32shxgUrU7v0CTnVQSMCAQGhRANCAARnSWJyCS5vEs2EWa0A8TmRpXHjpHh1zuhy9NXKrriKp+H23jXglBKZNPZyoJaevaAzputndyRz5i+ZCk0vhNVg");
        privateKeyMap.put("app_100800095600032", Sm2Utils.string2PrivateKey(privateKeyStrMap.get("app_100800095600032")));
        supportCertMap.put("app_100800095600032", CertTypeEnum.SM2);
        securityMap.put("app_100800095600032", "YOP-SM2-SM3");
        encryptKeyStrMap.put("app_100800095600032", "YURqdEFFY2NSNDFyQjE2cA==");
    }

    public static String getPrivateKeyStr(String appKey) {
        return privateKeyStrMap.get(appKey);
    }

    public static PrivateKey getPrivateKey(String appKey) {
        return privateKeyMap.get(appKey);
    }

    public static CertTypeEnum getSupportCertType(String appKey) {
        return supportCertMap.get(appKey);
    }

    public static Collection getApps() {
        List result = new LinkedList<>();
        for (String key : securityMap.keySet()) {
            result.add(new Object[]{key, securityMap.get(key)});
        }
        return result;
    }

    public static String getEncryptKeyStr(String appKey) {
        return encryptKeyStrMap.get(appKey);
    }
}
