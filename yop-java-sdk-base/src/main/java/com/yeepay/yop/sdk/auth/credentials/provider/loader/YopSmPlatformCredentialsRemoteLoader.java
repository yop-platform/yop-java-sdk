/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider.loader;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.cache.YopCertificateCache;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021-02-25
 */
public class YopSmPlatformCredentialsRemoteLoader implements YopPlatformCredentialsLoader {

    @Override
    public Map<String, YopPlatformCredentials> load(String appKey, String serialNo) {
        final X509Certificate latestPlatformCert = YopCertificateCache.loadPlatformSm2Certs(appKey, serialNo);
        if (null == latestPlatformCert) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(latestPlatformCert
                .getSerialNumber().toString(), new YopPlatformCredentialsHolder()
                .withSerialNo(latestPlatformCert.getSerialNumber().toString())
                .withPublicKey(CertTypeEnum.SM2, latestPlatformCert.getPublicKey()));
    }
}
