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

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;
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
        return getCredentials(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, serialNo, serverRoot);
    }

    private String cacheKey(String provider, String env, String appKey, String serialNo) {
        return StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER) + COLON
                + StringUtils.defaultString(env, YOP_DEFAULT_ENV) + COLON
                + appKey + COLON + serialNo;
    }

    @Override
    public YopPlatformCredentials getCredentials(String provider, String env, String appKey, String serialNo, String serverRoot) {
        if (StringUtils.isBlank(serialNo)) {
            throw new YopClientException("ReqParam Illegal, PlatformCert SerialNo NotSpecified");
        }
        String key = cacheKey(provider, env, appKey, serialNo);
        YopPlatformCredentials foundCredentials = credentialsMap.computeIfAbsent(key, p -> {
            if (serialNo.equals(YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO)) {
                PublicKey rsaPublicKey = loadLocalRsaKey(provider, env, appKey, serialNo);
                if (null == rsaPublicKey) {
                    throw new YopClientException("ConfigProblem, LocalRsaCert NotFound, " +
                            "provider:" + provider + ",env:" + env + ",serialNo:" + serialNo);
                }
                return convertCredentials(appKey, CertTypeEnum.RSA2048.getValue(), rsaPublicKey, YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO);
            } else {
                YopPlatformCredentials localCredentials = loadCredentialsFromStore(provider, env, appKey, serialNo);
                if (null == localCredentials) {
                    X509Certificate remoteCert = loadRemoteSm2Cert(provider, env, appKey, serialNo, serverRoot);
                    if (null == remoteCert) {
                        throw new YopClientException("ConfigProblem, LocalRsaCert NotFound, " +
                                "provider:" + provider + ",env:" + env + ",serialNo:" + serialNo);
                    }
                    return storeCredentials(provider, env, appKey, CertTypeEnum.SM2.name(), remoteCert);
                } else {
                    return localCredentials;
                }
            }
        });

        if (null != foundCredentials) {
            String realSerialNo = foundCredentials.getSerialNo();
            if (!StringUtils.equals(realSerialNo, serialNo)) {
                final String realKey = cacheKey(provider, env, appKey, realSerialNo);
                credentialsMap.put(realKey, foundCredentials);
            }
        }
        return foundCredentials;
    }

    /**
     * 从store加载国密证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号(长度为10的16进制字符串)
     * @return YopPlatformCredentials
     */
    protected abstract YopPlatformCredentials loadCredentialsFromStore(String appKey, String serialNo);

    protected YopPlatformCredentials loadCredentialsFromStore(String provider, String env, String appKey, String serialNo) {
        return loadCredentialsFromStore(appKey, serialNo);
    }


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
     * @param serverRoot 平台证书请求端点
     * @return X509Certificate
     */
    protected X509Certificate loadRemoteSm2Cert(String appKey, String serialNo, String serverRoot) {
        return loadRemoteSm2Cert(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, serialNo, serverRoot);
    }

    protected X509Certificate loadRemoteSm2Cert(String provider, String env, String appKey, String serialNo, String serverRoot) {
        final List<X509Certificate> x509Certificates = YopCertificateCache.loadPlatformSm2Certs(provider, env, appKey, serialNo, serverRoot);
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
        return loadLocalRsaCert(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, serialNo);
    }

    protected X509Certificate loadLocalRsaCert(String provider, String env, String appKey, String serialNo) {
        return YopCertificateCache.getYopPlatformRsaCertFromLocal(provider, env, appKey, serialNo);
    }

    protected PublicKey loadLocalRsaKey(String provider, String env, String appKey, String serialNo) {
        return YopCertificateCache.getYopPlatformRsaKeyFromLocal(provider, env, appKey, serialNo);
    }

    @Override
    public YopPlatformCredentials getLatestCredentials(String appKey, String credentialType) {
        return getLatestCredentials(appKey, credentialType, null);
    }

    @Override
    public YopPlatformCredentials getLatestCredentials(String appKey, String credentialType, String serverRoot) {
        return getLatestCredentials(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, credentialType, serverRoot);
    }

    @Override
    public YopPlatformCredentials getLatestCredentials(String provider, String env, String appKey, String credentialType, String serverRoot) {
        try {
            // rsa
            final CertTypeEnum parsedCertType = CertTypeEnum.parse(credentialType);
            if (CertTypeEnum.RSA2048.equals(parsedCertType)) {
                return getCredentials(appKey, YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO, serverRoot);
            }

            // sm2
            return getSm2Credentials(provider, env, appKey, parsedCertType, serverRoot);
        } catch (Exception e) {
            LOGGER.warn("getLatestCredentials error, ex:", e);
        }
        LOGGER.warn("No YopPlatformCredentials Found For provider:{}, env:{}, appKey:{}, credentialType:{}",
                provider, env, appKey, credentialType);
        return null;
    }

    private YopPlatformCredentials getSm2Credentials(String provider, String env, String appKey, CertTypeEnum parsedCertType, String serverRoot) {
        List<X509Certificate> loadedCerts;
        try {
            loadedCerts = YopCertificateCache.loadPlatformSm2Certs(provider, env, appKey, EMPTY, serverRoot);
            // 临期：异步刷新
            if (CollectionUtils.isNotEmpty(loadedCerts) && X509CertUtils.checkCertDate(provider, env, loadedCerts.get(0))) {
                YopCertificateCache.refreshPlatformSm2Certs(provider, env, appKey, EMPTY, serverRoot);
            }
        } catch (CertificateException e) {
            // 过期：同步加载
            LOGGER.warn("YopPlatformCredentials expired and need reload, appKey:" + appKey + ", credentialType:" + parsedCertType + ", ex", e);
            loadedCerts = YopCertificateCache.reloadPlatformSm2Certs(provider, env, appKey, EMPTY, serverRoot);
        }

        if (CollectionUtils.isEmpty(loadedCerts)) {
            LOGGER.warn("No YopPlatformCredentials Found For appKey:{}, credentialType:{}", appKey, parsedCertType);
            return null;
        }

        X509Certificate latestCert = loadedCerts.get(0);
        YopPlatformCredentials credentials = storeCredentials(provider, env, appKey, CertTypeEnum.SM2.name(), latestCert);
        credentialsMap.put(cacheKey(provider, env, appKey, credentials.getSerialNo()), credentials);
        return credentials;
    }

    @Override
    public YopPlatformCredentials storeCredentials(String provider, String env, String appKey, String credentialType, X509Certificate certificate) {
        return storeCredentials(appKey, credentialType, certificate);
    }

    /**
     * 将证书转换为凭证
     *
     * @param credentialType 凭证类型
     * @param cert           证书
     * @return YopPlatformCredentials
     */
    protected YopPlatformCredentials convertCredentials(String appKey, String credentialType, X509Certificate cert) {
        if (null == cert) return null;
        final CertTypeEnum certType = CertTypeEnum.parse(credentialType);
        return new YopPlatformCredentialsHolder().withCredentials(new PKICredentialsItem(cert.getPublicKey(), certType))
                .withSerialNo(X509CertUtils.parseToHex(cert.getSerialNumber().toString())).withAppKey(appKey);
    }

    /**
     * 将普通公钥转换为凭证
     *
     * @param appKey 应用
     * @param credentialType 密钥类型
     * @param publicKey 公钥
     * @param keyId 凭证标识
     * @return YopPlatformCredentials
     */
    protected YopPlatformCredentials convertCredentials(String appKey, String credentialType, PublicKey publicKey, String keyId) {
        if (null == publicKey) return null;
        final CertTypeEnum certType = CertTypeEnum.parse(credentialType);
        return new YopPlatformCredentialsHolder().withCredentials(new PKICredentialsItem(publicKey, certType))
                .withSerialNo(keyId).withAppKey(appKey);
    }
}
