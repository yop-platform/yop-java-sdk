/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider.loader;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.security.PublicKey;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider.YOP_CERT_RSA_DEFAULT_SERIAL_NO;

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
public class YopRsaPlatformCredentialsLoader implements YopPlatformCredentialsLoader {

    private Map<String, YopPlatformCredentials> credentialsMap = new ConcurrentHashMap<>();

    @Override
    public synchronized Map<String, YopPlatformCredentials> load(String appKey, String serialNo) {
        if (!credentialsMap.containsKey(YOP_CERT_RSA_DEFAULT_SERIAL_NO)) {
            reload(appKey, serialNo);
        }
        return Collections.unmodifiableMap(credentialsMap);
    }

    @Override
    public Map<String, YopPlatformCredentials> reload(String appKey, String serialNo) {
        final PublicKey rsaPublicKey = YopSdkConfigProviderRegistry.getProvider().getConfig().loadYopPublicKey(CertTypeEnum.RSA2048);
        if (null != rsaPublicKey) {
            credentialsMap.put(YOP_CERT_RSA_DEFAULT_SERIAL_NO, new YopPlatformCredentialsHolder()
                    .withSerialNo(YOP_CERT_RSA_DEFAULT_SERIAL_NO).withPublicKey(CertTypeEnum.RSA2048, rsaPublicKey));
        }
        return Collections.unmodifiableMap(credentialsMap);
    }
}
