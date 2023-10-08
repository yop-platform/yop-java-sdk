/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.credentials.provider;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.base.cache.YopCertificateCache;
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

import static com.yeepay.yop.sdk.YopConstants.YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;

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

    /**
     * serialNo -> YopPlatformCredentials
     */
    protected Map<String, YopPlatformCredentials> credentialsMap = Maps.newConcurrentMap();

    @Override
    public YopPlatformCredentials getCredentials(String appKey, String serialNo) {
        return getCredentials(appKey, serialNo, null);
    }

    @Override
    public YopPlatformCredentials getCredentials(String appKey, String serialNo, String serverRoot) {
        if (StringUtils.isBlank(serialNo)) {
            throw new YopClientException("ReqParam Illegal, PlatformCert SerialNo NotSpecified");
        }
        YopPlatformCredentials foundCredentials = credentialsMap.computeIfAbsent(serialNo, p -> {
            if (serialNo.equals(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO)) {
                X509Certificate rsaCert = loadLocalRsaCert(appKey, serialNo);
                if (null == rsaCert) {
                    throw new YopClientException("ConfigProblem, LocalRsaCert NotFound, serialNo:" + serialNo);
                }
                return convertRsaCredentials(appKey, CertTypeEnum.RSA2048, rsaCert);
            } else {
                YopPlatformCredentials localCredentials = loadCredentialsFromStore(appKey, serialNo);
                if (null == localCredentials) {
                    X509Certificate remoteCert = loadRemoteSm2Cert(appKey, serialNo, serverRoot);
                    if (null == remoteCert) {
                        throw new YopClientException("ConfigProblem, RemoteSm2Cert NotFound, serialNo:" + serialNo);
                    }
                    return storeCredentials(appKey, CertTypeEnum.SM2.name(), remoteCert);
                } else {
                    return localCredentials;
                }
            }
        });

        if (null != foundCredentials) {
            String realSerialNo = foundCredentials.getSerialNo();
            if (!StringUtils.equals(realSerialNo, serialNo)) {
                credentialsMap.put(realSerialNo, foundCredentials);
            }
        }
        return foundCredentials;
    }

    private YopPlatformCredentials convertRsaCredentials(String appKey, CertTypeEnum certType, X509Certificate cert) {
        return new YopPlatformCredentialsHolder().withAppKey(appKey)
                .withSerialNo(X509CertUtils.parseToHex(cert.getSerialNumber().toString()))
                .withCredentials(new PKICredentialsItem(cert.getPublicKey(), certType));
    }

    /**
     * 从store加载证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号(长度为10的16进制字符串)
     * @return YopPlatformCredentials
     */
    protected abstract YopPlatformCredentials loadCredentialsFromStore(String appKey, String serialNo);


    /**
     * 从远端加载指定序列号国密证书
     *
     * @param appKey     应用标识
     * @param serialNo   证书序列号(长度为10的16进制字符串)
     * @return X509Certificate
     */
    protected X509Certificate loadRemoteSm2Cert(String appKey, String serialNo) {
        return loadRemoteSm2Cert(appKey, serialNo, null);
    }

    /**
     * 从远端加载指定序列号国密证书
     *
     * @param appKey     应用标识
     * @param serialNo   证书序列号(长度为10的16进制字符串)
     * @param serverRoot 请求端点
     * @return X509Certificate
     */
    protected X509Certificate loadRemoteSm2Cert(String appKey, String serialNo, String serverRoot) {
        final List<X509Certificate> x509Certificates = YopCertificateCache.loadPlatformSm2Certs(appKey, serialNo, serverRoot);
        if (CollectionUtils.isNotEmpty(x509Certificates)) {
            return x509Certificates.get(0);
        }
        return null;
    }

    /**
     * 读取内置RSA证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号(长度为10的16进制字符串)
     * @return X509Certificate
     */
    protected X509Certificate loadLocalRsaCert(String appKey, String serialNo) {
        return YopCertificateCache.getYopPlatformRsaCertFromLocal();
    }

    @Override
    public YopPlatformCredentials getLatestCredentials(String appKey, String credentialType) {
        return getLatestCredentials(appKey, credentialType, null);
    }

    @Override
    public YopPlatformCredentials getLatestCredentials(String appKey, String credentialType, String serverRoot) {
        try {
            // rsa
            final CertTypeEnum parsedCertType = CertTypeEnum.parse(credentialType);
            if (CertTypeEnum.RSA2048.equals(parsedCertType)) {
                return getCredentials(appKey, YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO);
            }

            // sm2
            return getSm2Credentials(appKey, parsedCertType, serverRoot);
        } catch (Exception e) {
            LOGGER.warn("getLatestCredentials error, ex:", e);
        }
        LOGGER.warn("No YopPlatformCredentials Found For appKey:{}, credentialType:{}", appKey, credentialType);
        return null;
    }

    private YopPlatformCredentials getSm2Credentials(String appKey, CertTypeEnum parsedCertType, String serverRoot) {
        List<X509Certificate> loadedCerts;
        try {
            loadedCerts = YopCertificateCache.loadPlatformSm2Certs(appKey, EMPTY, serverRoot);
            // 临期：异步刷新
            if (CollectionUtils.isNotEmpty(loadedCerts) && X509CertUtils.checkCertDate(loadedCerts.get(0))) {
                YopCertificateCache.refreshPlatformSm2Certs(appKey, EMPTY, serverRoot);
            }
        } catch (CertificateException e) {
            // 过期：同步加载
            LOGGER.warn("YopPlatformCredentials expired and need reload, appKey:" + appKey + ", credentialType:" + parsedCertType + ", ex", e);
            loadedCerts = YopCertificateCache.reloadPlatformSm2Certs(appKey, EMPTY, serverRoot);
        }

        if (CollectionUtils.isEmpty(loadedCerts)) {
            LOGGER.warn("No YopPlatformCredentials Found For appKey:{}, credentialType:{}", appKey, parsedCertType);
            return null;
        }

        X509Certificate latestCert = loadedCerts.get(0);
        YopPlatformCredentials credentials = storeCredentials(appKey, CertTypeEnum.SM2.name(), latestCert);
        credentialsMap.put(credentials.getSerialNo(), credentials);
        return credentials;
    }
}
