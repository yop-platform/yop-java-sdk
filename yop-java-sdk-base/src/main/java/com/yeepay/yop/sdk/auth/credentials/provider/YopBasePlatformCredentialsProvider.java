/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.cache.YopCertificateCache;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO;
import static com.yeepay.yop.sdk.utils.CharacterConstants.EMPTY;

/**
 * title: Yop平台凭证基础类<br>
 * description: 将load逻辑抽象到此类，方便复用<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:30 PM
 */
public abstract class YopBasePlatformCredentialsProvider implements YopPlatformCredentialsProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(YopBasePlatformCredentialsProvider.class);

    protected static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(200),
            new ThreadFactoryBuilder().setNameFormat("yop-platform-cert-store-task-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * serialNo -> YopPlatformCredentials
     */
    protected Map<String, YopPlatformCredentials> credentialsMap = Maps.newConcurrentMap();

    @Override
    public YopPlatformCredentials getYopPlatformCredentials(String appKey, String serialNo) {
        if (StringUtils.isBlank(serialNo)) {
            throw new YopClientException("serialNo is required");
        }
        YopPlatformCredentials foundCredentials = credentialsMap.get(serialNo);
        if (null == foundCredentials) {
            if (serialNo.equals(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO)) {
                foundCredentials = toCredentials(appKey, CertTypeEnum.RSA2048, loadLocalRsaCert(appKey, serialNo));
            } else {
                foundCredentials = loadCredentialsFromStore(appKey, serialNo);
                if (null == foundCredentials) {
                    foundCredentials = toCredentials(appKey, CertTypeEnum.SM2, loadRemoteSm2Cert(appKey, serialNo));
                }
            }
        }
        if (null != foundCredentials) {
            String realSerialNo = foundCredentials.getSerialNo();
            credentialsMap.put(serialNo, foundCredentials);
            if (!StringUtils.equals(realSerialNo, serialNo)) {
                credentialsMap.put(realSerialNo, foundCredentials);
            }
        }
        return foundCredentials;
    }

    /**
     * 从store加载证书
     *
     * @param appKey
     * @param serialNo
     * @return
     */
    protected abstract YopPlatformCredentials loadCredentialsFromStore(String appKey, String serialNo);


    /**
     * 从远端加载国密证书并存入store
     *
     * @param appKey
     * @param serialNo
     * @return
     */
    protected X509Certificate loadRemoteSm2Cert(String appKey, String serialNo) {
        final List<X509Certificate> x509Certificates = YopCertificateCache.loadPlatformSm2Certs(appKey, serialNo);
        if (CollectionUtils.isNotEmpty(x509Certificates)) {
            Map<String, X509Certificate> certificateMap = Maps.newHashMapWithExpectedSize(x509Certificates.size());
            x509Certificates.forEach(p -> certificateMap.put(p.getSerialNumber().toString(), p));

            // 异步存入本地
            saveCertsIntoStoreAsync(certificateMap);
            return certificateMap.get(serialNo);
        }
        return null;
    }

    protected void saveCertsIntoStoreAsync(Map<String, X509Certificate> certificateMap) {
        THREAD_POOL.submit(() -> {
            try {
                saveCertsIntoStore(certificateMap);
            } catch (Exception e) {
                LOGGER.warn("error when storeCerts, ex:", e);
            }
        });
    }

    /**
     * 读取内置证书
     *
     * @param appKey
     * @param serialNo
     * @return
     */
    protected X509Certificate loadLocalRsaCert(String appKey, String serialNo) {
        return YopCertificateCache.getYopPlatformRsaCertFromLocal();
    }

    @Override
    public YopPlatformCredentials getLatestAvailable(String appKey, String credentialType) {
        try {
            switch (CertTypeEnum.parse(credentialType)) {
                case SM2:
                    X509Certificate latestCert;
                    try {
                        latestCert = YopCertificateCache.loadPlatformSm2Certs(appKey, EMPTY).get(0);
                        // 临期：异步刷新
                        if (X509CertUtils.checkCertDate(latestCert)) {
                            latestCert = YopCertificateCache.refreshPlatformSm2Certs(appKey, EMPTY).get(0);
                        }
                    } catch (CertificateException e) {
                        LOGGER.warn("YopPlatformCredentials expired and need reload, appKey:" + appKey + ", credentialType:" + credentialType + ", ex", e);
                        // 过期：同步加载
                        latestCert = YopCertificateCache.reloadPlatformSm2Certs(appKey, EMPTY).get(0);
                    }

                    YopPlatformCredentials credentials = toCredentials(appKey, CertTypeEnum.SM2, latestCert);
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

    /**
     * 将证书转换为凭证(加密机需要实现)
     *
     * @param certType
     * @param cert
     * @return
     */
    protected YopPlatformCredentials toCredentials(String appKey, CertTypeEnum certType, X509Certificate cert) {
        if (null == cert) return null;
        return new YopPlatformCredentialsHolder().withCredentials(new PKICredentialsItem(cert.getPublicKey(), certType))
                .withSerialNo(cert.getSerialNumber().toString()).withAppKey(appKey);
    }
}