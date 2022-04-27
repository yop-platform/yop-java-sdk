/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.model.cert.YopPlatformCertQueryResult;
import com.yeepay.yop.sdk.model.cert.YopPlatformPlainCert;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.utils.CharacterConstants.COMMA;

/**
 * title: Yop证书缓存类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/26
 */
public class YopCertificateCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCertificateCache.class);

    private static final String CERT_DOWNLOAD_API_URI = "/rest/v1.0/yop/yop-platform-cert-query/find-plain-cert";
    private static final String CERT_DOWNLOAD_API_METHOD = "GET";
    private static final String CERT_DOWNLOAD_API_SECURITY = "YOP-SM2-SM3";
    private static final String CERT_DOWNLOAD_API_PARAM_SERIAL_NO = "serialNo";
    private static final String CERT_DOWNLOAD_API_PARAM_CERT_TYPE = "certType";

    // 证书缓存24小时
    private static final LoadingCache<String, X509Certificate> PLATFORM_CERT_CACHE = initCache(24L, TimeUnit.HOURS);

    private static YopClient YOP_CLIENT;

    private static X509Certificate CFCA_ROOT_CERT, YOP_INTER_CERT;

    /**
     * 本地加载cfca根证书
     *
     * @return X509Certificate
     */
    public static X509Certificate getCfcaRootCertFromLocal() {
        return CFCA_ROOT_CERT;
    }

    /**
     * 本地加载YOP中间证书
     *
     * @return X509Certificate
     */
    public static X509Certificate getYopInterCertFromLocal() {
        return CFCA_ROOT_CERT;
    }

    /**
     * 加载平台证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号，可空
     * @return 最新平台证书
     */
    public static X509Certificate loadPlatformSm2Certs(String appKey, String serialNo) {
        final String cacheKey = getCacheKey(appKey, serialNo);
        return loadPlatformSm2Certs(cacheKey);
    }

    private static X509Certificate loadPlatformSm2Certs(String cacheKey) {
        try {
            return PLATFORM_CERT_CACHE.get(cacheKey);
        } catch (Exception e) {
            LOGGER.warn("UnexpectedException occurred when load platformCert for cacheKey:" + cacheKey, e);
        }
        return null;
    }

    /**
     * 加载并异步刷新平台证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号，可空
     * @return 最新平台证书
     */
    public static X509Certificate refreshPlatformSm2Certs(String appKey, String serialNo) {
        final String cacheKey = getCacheKey(appKey, serialNo);
        try {
            // async
            PLATFORM_CERT_CACHE.refresh(cacheKey);
        } catch (Exception e) {
            LOGGER.warn("UnexpectedException occurred when refresh platformCert for cacheKey:" + cacheKey, e);
        }
        return loadPlatformSm2Certs(cacheKey);
    }

    /**
     * 失效并重新加载平台证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号，可空
     * @return 最新平台证书
     */
    public static X509Certificate reLoadPlatformSm2Certs(String appKey, String serialNo) {
        final String cacheKey = getCacheKey(appKey, serialNo);
        try {
            PLATFORM_CERT_CACHE.invalidate(cacheKey);
        } catch (Exception e) {
            LOGGER.warn("UnexpectedException occurred when invalidate platformCert for cacheKey:" + cacheKey, e);
        }
        return loadPlatformSm2Certs(cacheKey);
    }

    private static String getCacheKey(String appKey, String serialNo) {
        appKey = StringUtils.defaultIfBlank(appKey, YopConstants.YOP_DEFAULT_APPKEY);
        return StringUtils.isBlank(serialNo) ? appKey : StringUtils.joinWith(COMMA, appKey, serialNo);
    }

    private static synchronized X509Certificate doLoad(YopCredentials<?> yopCredentials, String serialNo) {
        X509Certificate result = null;
        try {
            YopRequest request = new YopRequest(CERT_DOWNLOAD_API_URI, CERT_DOWNLOAD_API_METHOD);
            // 跳过验签、加解密
            request.getRequestConfig().setSkipVerifySign(true)
                    .setNeedEncrypt(false)
                    .setSecurityReq(CERT_DOWNLOAD_API_SECURITY)
                    .setCredentials(yopCredentials);
            if (StringUtils.isNotBlank(serialNo)) {
                request.addParameter(CERT_DOWNLOAD_API_PARAM_SERIAL_NO, serialNo);
            }
            request.addParameter(CERT_DOWNLOAD_API_PARAM_CERT_TYPE, CertTypeEnum.SM2.getValue());
            YopResponse response = YOP_CLIENT.request(request);

            // 响应解析
            List<YopPlatformPlainCert> platformCerts = parseYopResponse(response);

            // 证书验证
            List<X509Certificate> legalPlatformCerts = checkCerts(platformCerts);

            result = CollectionUtils.isEmpty(legalPlatformCerts) ? null : legalPlatformCerts.get(0);
        } catch (Exception e) {
            LOGGER.error("error when load sm2 cert from remote, ex:", e);
        }
        return result;
    }

    private static List<YopPlatformPlainCert> parseYopResponse(YopResponse response) {
        try {
            final YopPlatformCertQueryResult bizResponse = new YopPlatformCertQueryResult();
            JsonUtils.load(response.getStringResult(), bizResponse);
            return bizResponse.getData();
        } catch (Exception e) {
            LOGGER.error("error when load sm2 cert, ex:", e);
        }
        return Collections.emptyList();
    }

    private static List<X509Certificate> checkCerts(List<YopPlatformPlainCert> platformCerts) {
        if (CollectionUtils.isNotEmpty(platformCerts)) {
            List<X509Certificate> result = Lists.newArrayList();
            for (YopPlatformPlainCert platformCert : platformCerts) {
                X509Certificate tmp = decodeCert(platformCert);
                if (null != tmp) {
                    tmp = verifyCert(tmp);
                }
                if (null != tmp) {
                    result.add(tmp);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }


    private static X509Certificate decodeCert(YopPlatformPlainCert platformCert) {
        try {
            byte[] certBytes = platformCert.getCert().getBytes(YopConstants.DEFAULT_ENCODING);
            return Sm2CertUtils.getX509Certificate(certBytes);
        } catch (Exception e) {
            LOGGER.warn("fail to decode platform cert:" + platformCert + ", ex:", e);
        }
        return null;
    }

    private static X509Certificate verifyCert(X509Certificate platformCert) {
        try {
            Sm2CertUtils.verifyCertificate((BCECPublicKey) YOP_INTER_CERT.getPublicKey(), platformCert);
            return platformCert;
        } catch (Exception e) {
            LOGGER.error("error to verify platform cert:" + platformCert + ", ex:", e);
        }
        return null;
    }

    static {
        try {
            String cfcaRootFile = DEFAULT_CFCA_ROOT_FILE, yopInterFile = DEFAULT_YOP_INTER_FILE;
            if (!EnvUtils.isProd()) {
                String env = EnvUtils.currentEnv(),
                        envPrefix = StringUtils.substringBefore(env, "_");
                cfcaRootFile = envPrefix + "_" + DEFAULT_CFCA_ROOT_FILE;
                yopInterFile = envPrefix + "_" + DEFAULT_YOP_INTER_FILE;
            }

            // 根证书
            CFCA_ROOT_CERT = getX508Cert(DEFAULT_CERT_PATH + "/" + cfcaRootFile);
            Sm2CertUtils.verifyCertificate(null, CFCA_ROOT_CERT);

            // 中间证书
            YOP_INTER_CERT = getX508Cert(DEFAULT_CERT_PATH + "/" + yopInterFile);
            Sm2CertUtils.verifyCertificate((BCECPublicKey) CFCA_ROOT_CERT.getPublicKey(), YOP_INTER_CERT);
        } catch (Exception e) {
            LOGGER.error("error when load parent certs, ex:", e);
        }
        YOP_CLIENT = YopClientBuilder.builder().build();
    }

    private static X509Certificate getX508Cert(String certPath) throws CertificateException, NoSuchProviderException {
        InputStream certStream = null;
        try {
            certStream = FileUtils.getResourceAsStream(certPath);
            return Sm2CertUtils.getX509Certificate(certStream);
        } finally {
            StreamUtils.closeQuietly(certStream);
        }
    }

    private static LoadingCache<String, X509Certificate> initCache(Long expire, TimeUnit timeUnit) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (expire > 0) {
            cacheBuilder.expireAfterWrite(expire, timeUnit);
        }
        return cacheBuilder.build(new CacheLoader<String, X509Certificate>() {
            @Override
            public X509Certificate load(String cacheKey) throws Exception {
                LOGGER.debug("try to init platform cert for cacheKey:" + cacheKey);
                X509Certificate platformCert = null;
                try {
                    String[] split = cacheKey.split(COMMA);
                    String appKey = split[0], serialNo = split.length > 1 ? split[1] : null;
                    platformCert = doLoad(YopCredentialsProviderRegistry.getProvider()
                            .getCredentials(appKey, CertTypeEnum.SM2.getValue()), serialNo);
                } catch (Exception ex) {
                    LOGGER.warn("UnexpectedException occurred when init platformCert for cacheKey:" + cacheKey, ex);
                }
                return platformCert;
            }
        });
    }

}
