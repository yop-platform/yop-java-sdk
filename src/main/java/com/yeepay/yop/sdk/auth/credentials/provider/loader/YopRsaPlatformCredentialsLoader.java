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
import java.util.HashMap;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO;

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

    @Override
    public Map<String, YopPlatformCredentials> load(String appKey, String serialNo) {
        final PublicKey rsaPublicKey = YopSdkConfigProviderRegistry.getProvider().getConfig().loadYopPublicKey(CertTypeEnum.RSA2048);
        return new HashMap<String, YopPlatformCredentials>(4) {{
            put(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO, new YopPlatformCredentialsHolder()
                    .withSerialNo(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO)
                    .withPublicKey(CertTypeEnum.RSA2048, rsaPublicKey));
        }};
    }

}
