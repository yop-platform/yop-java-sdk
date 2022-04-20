/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopPlatformCredentialsLoader;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopRsaPlatformCredentialsLoader;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopSmPlatformCredentialsLocalLoader;
import com.yeepay.yop.sdk.exception.YopServiceException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.yeepay.yop.sdk.YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO;
import static com.yeepay.yop.sdk.YopConstants.YOP_SM_PLATFORM_CERT_DEFAULT_SERIAL_NO;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:30 PM
 */
public class YopFilePlatformCredentialsProvider implements YopPlatformCredentialsProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(YopFilePlatformCredentialsProvider.class);

    /**
     * serialNo -> YopPlatformCredentials
     */
    private Map<String, YopPlatformCredentials> credentialsMap = new ConcurrentHashMap<>();

    /**
     * type -> YopPlatformCredentialsLoader
     */
    private Map<String, YopPlatformCredentialsLoader> yopPlatformCredentialsLoaderMap = Maps.newHashMapWithExpectedSize(2);

    public YopFilePlatformCredentialsProvider() {
        this.yopPlatformCredentialsLoaderMap.put(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO, new YopRsaPlatformCredentialsLoader());
        this.yopPlatformCredentialsLoaderMap.put(YOP_SM_PLATFORM_CERT_DEFAULT_SERIAL_NO, new YopSmPlatformCredentialsLocalLoader());
    }

    @Override
    public YopPlatformCredentials getYopPlatformCredentials(String appKey, String serialNo) {
        if (StringUtils.isBlank(serialNo)) {
            throw new YopServiceException("serialNo is required");
        }

        YopPlatformCredentials foundCredentials = credentialsMap.get(serialNo);
        if (null == foundCredentials) {
            String yopPlatformLoader = YOP_SM_PLATFORM_CERT_DEFAULT_SERIAL_NO;
            if (serialNo.equals(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO)) {
                yopPlatformLoader = serialNo;
            }

            Map<String, YopPlatformCredentials> yopPlatformCredentials = yopPlatformCredentialsLoaderMap.get(yopPlatformLoader).load(appKey, serialNo);
            if (MapUtils.isNotEmpty(yopPlatformCredentials)) {
                yopPlatformCredentials.forEach(credentialsMap::put);
            }
            if (yopPlatformCredentials.containsKey(serialNo)) {
                return yopPlatformCredentials.get(serialNo);
            }
        }
        return foundCredentials;
    }

    @Override
    public Map<String, YopPlatformCredentials> reload(String appKey, String serialNo) {
        for (Map.Entry<String, YopPlatformCredentialsLoader> entry : yopPlatformCredentialsLoaderMap.entrySet()) {
            YopPlatformCredentialsLoader loader = entry.getValue();
            Map<String, YopPlatformCredentials> yopPlatformCredentials = loader.load(appKey, serialNo);
            if (MapUtils.isNotEmpty(yopPlatformCredentials)) {
                credentialsMap.putAll(yopPlatformCredentials);
            }
        }
        return Collections.unmodifiableMap(credentialsMap);
    }

}
