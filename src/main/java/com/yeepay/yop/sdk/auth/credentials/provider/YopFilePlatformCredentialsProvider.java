/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopPlatformCredentialsLoader;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopRsaPlatformCredentialsLoader;
import com.yeepay.yop.sdk.auth.credentials.provider.loader.YopSm2PlatformCredentialsLoader;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.Sm2CertUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private YopPlatformCredentialsLoader rsaDelegate = new YopRsaPlatformCredentialsLoader();
    private YopPlatformCredentialsLoader sm2Delegate = new YopSm2PlatformCredentialsLoader();

    private Map<String, YopPlatformCredentials> credentialsMap = new ConcurrentHashMap<>();

    protected static X509Certificate cfcaRoot, yopInter;
    {
        try {
            cfcaRoot = Sm2CertUtils.getX509Certificate(FileUtils.getResourceAsStream("config/certs/cfca_root.pem"));
            try {
                Sm2CertUtils.verifyCertificate(null, cfcaRoot);
            } catch (Exception e) {
                throw new YopClientException("invalid cfca root cert, detail:" + e.getMessage());
            }

            yopInter = Sm2CertUtils.getX509Certificate(FileUtils.getResourceAsStream("config/certs/yop_inter.pem"));
            try {
                Sm2CertUtils.verifyCertificate((BCECPublicKey) cfcaRoot.getPublicKey(), yopInter);
            } catch (Exception e) {
                throw new YopClientException("invalid yop inter cert, detail:" + e.getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("error when load parent certs, ex:", e);
        }
    }

    @Override
    public YopPlatformCredentials getCredentials(String appKey, String serialNo) {
        if (StringUtils.isBlank(serialNo)) {
            throw new YopServiceException("serialNo is required");
        }
        YopPlatformCredentials foundCredentials = credentialsMap.get(serialNo);
        if (null == foundCredentials) {
            // RSA
            Map<String, YopPlatformCredentials> rsaCredentials = rsaDelegate.load(appKey, serialNo);
            if (MapUtils.isNotEmpty(rsaCredentials)) {
                rsaCredentials.forEach(credentialsMap::put);
            }
            if (rsaCredentials.containsKey(serialNo)) {
                return rsaCredentials.get(serialNo);
            }
            // SM2
            Map<String, YopPlatformCredentials> sm2Credentials = load(appKey, serialNo);
            if (MapUtils.isNotEmpty(sm2Credentials)) {
                sm2Credentials.forEach(credentialsMap::put);
            }
            if (sm2Credentials.containsKey(serialNo)) {
                return sm2Credentials.get(serialNo);
            }
        }
        return foundCredentials;
    }

    @Override
    public Map<String, YopPlatformCredentials> reload(String appKey, String serialNo) {
        // 1.加载RSA公钥
        Map<String, YopPlatformCredentials> rsaCredentials = rsaDelegate.reload(appKey, serialNo);
        if (MapUtils.isNotEmpty(rsaCredentials)) {
            credentialsMap.putAll(rsaCredentials);
        }
        // 2.加载SM2证书
        Map<String, YopPlatformCredentials> sm2Credentials = sm2Delegate.reload(appKey, serialNo);
        if (MapUtils.isNotEmpty(sm2Credentials)) {
            credentialsMap.putAll(sm2Credentials);
        }
        return Collections.unmodifiableMap(credentialsMap);
    }

    private Map<String, YopPlatformCredentials> load(String appKey, String serialNo) {
        YopCertStore yopCertStore = YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore();
        Map<String, X509Certificate> certificateMap = loadAndVerifyFromLocal(yopCertStore);
        Map<String, YopPlatformCredentials> localCredentials = new LinkedHashMap<>();
        if (MapUtils.isNotEmpty(certificateMap)) {
            certificateMap.forEach((serial, cert) -> localCredentials.put(serial, new YopPlatformCredentialsHolder()
                    .withSerialNo(serial).withPublicKey(CertTypeEnum.SM2, cert.getPublicKey())));
        } else {
            LOGGER.warn("no available sm2 cert from local, path:{}", yopCertStore.getPath());
        }
        if (localCredentials.containsKey(serialNo)) {
            return localCredentials;
        } else {
            return sm2Delegate.load(appKey, serialNo);
        }
    }

    private Map<String, X509Certificate> loadAndVerifyFromLocal(YopCertStore yopCertStore) {
        Map<String, X509Certificate> certMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(yopCertStore.getPath()) && BooleanUtils.isTrue(yopCertStore.getEnable())) {
            File certStoreDir = new File(yopCertStore.getPath());
            if (certStoreDir.exists() && certStoreDir.isDirectory()) {
                File[] certFiles = certStoreDir.listFiles();
                if (ArrayUtils.isNotEmpty(certFiles)) {
                    for (File certFile : certFiles) {
                        try {
                            final X509Certificate cert = Sm2CertUtils.getX509Certificate(new FileInputStream(certFile));
                            Sm2CertUtils.verifyCertificate((BCECPublicKey) yopInter.getPublicKey(), cert);
                            certMap.put(cert.getSerialNumber().toString(), cert);
                        } catch (Exception e) {
                            LOGGER.error("error when load cert from local file:" + certFile.getName() + ", ex:", e);
                        }
                    }
                }
            } else {
                LOGGER.warn("invalid path when load cert from local file, path:{}", yopCertStore.getPath());
            }
        }

        return certMap;
    }
}
