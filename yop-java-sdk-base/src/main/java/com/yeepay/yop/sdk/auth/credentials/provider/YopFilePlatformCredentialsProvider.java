/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopPlatformCredentialsLoader;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopRsaPlatformCredentialsLoader;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopSmPlatformCredentialsLocalLoader;
import com.yeepay.yop.sdk.cache.YopCertificateCache;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.Sm2CertUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.utils.CharacterConstants.EMPTY;

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
    private Map<String, YopPlatformCredentials> credentialsMap = Maps.newConcurrentMap();

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

            Map<String, YopPlatformCredentials> yopPlatformCredentials = yopPlatformCredentialsLoaderMap
                    .get(yopPlatformLoader).load(appKey, serialNo);
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

    @Override
    public YopPlatformCredentials getLatestAvailable(String appKey, String credentialType) {
        try {
            switch (CertTypeEnum.parse(credentialType)) {
                case SM2:
                    X509Certificate latestCert;
                    try {
                        latestCert = YopCertificateCache.loadPlatformSm2Certs(appKey, EMPTY);
                        // 临期：异步刷新
                        if (Sm2CertUtils.checkCertDate(latestCert)) {
                            latestCert = YopCertificateCache.refreshPlatformSm2Certs(appKey, EMPTY);
                        }
                    } catch (CertificateException e) {
                        LOGGER.warn("YopPlatformCredentials expired and need reload, appKey:" + appKey + ", credentialType:" + credentialType + ", ex", e);
                        // 过期：同步加载
                        latestCert = YopCertificateCache.reLoadPlatformSm2Certs(appKey, EMPTY);
                    }

                    YopPlatformCredentialsHolder credentials = toCredentials(latestCert);
                    credentialsMap.put(credentials.getSerialNo(), credentials);
                    return credentials;
                case RSA2048:
                    return getYopPlatformCredentials(appKey, YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO);
                default:
                    return null;
            }
        } catch (Exception e) {
            LOGGER.warn("no YopPlatformCredentials found for appKey:{}, credentialType:{}", appKey, credentialType);
        }
        return null;
    }

    private YopPlatformCredentialsHolder toCredentials(X509Certificate cert) {
        return new YopPlatformCredentialsHolder().withPublicKey(CertTypeEnum.SM2, cert.getPublicKey())
                .withSerialNo(cert.getSerialNumber().toString());
    }

    @Override
    public void storeCerts(Map<String, X509Certificate> plainCerts) {
        storeCerts(YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore(), plainCerts);
    }

    private void storeCerts(YopCertStore yopCertStore, Map<String, X509Certificate> plainCerts) {
        if (MapUtils.isEmpty(plainCerts)) {
            return;
        }
        // 本地存储
        plainCerts.entrySet().forEach(p -> credentialsMap.put(p.getKey(), toCredentials(p.getValue())));

        // 文件存储
        if (null == yopCertStore || !BooleanUtils.isTrue(yopCertStore.getEnable())) {
            return;
        }
        for (Map.Entry<String, X509Certificate> certificateEntry : plainCerts.entrySet()) {
            try {
                final File certStoreDir = new File(yopCertStore.getPath());
                if (!certStoreDir.exists()) {
                    certStoreDir.mkdirs();
                }
                final File certFile = new File(certStoreDir, YOP_SM_PLATFORM_CERT_PREFIX + certificateEntry.getKey() + YOP_PLATFORM_CERT_POSTFIX);
                JcaPEMWriter jcaPEMWriter = null;
                try {
                    jcaPEMWriter = new JcaPEMWriter(new FileWriter(certFile));
                    jcaPEMWriter.writeObject(new PemObject("CERTIFICATE", certificateEntry.getValue().getEncoded()));
                } finally {
                    StreamUtils.closeQuietly(jcaPEMWriter);
                }
            } catch (Exception e) {
                LOGGER.error("error when store yop cert, ex:", e);
            }
        }
    }

}
