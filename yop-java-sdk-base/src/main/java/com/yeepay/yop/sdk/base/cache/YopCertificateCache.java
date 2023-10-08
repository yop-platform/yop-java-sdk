/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory;
import com.yeepay.yop.sdk.client.YopGlobalClient;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.model.cert.YopPlatformCertQueryResult;
import com.yeepay.yop.sdk.model.cert.YopPlatformPlainCert;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.security.cert.YopPublicKey;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.EnvUtils;
import com.yeepay.yop.sdk.utils.JsonUtils;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COMMA;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;

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

    private static final String CERT_DOWNLOAD_API_URI = "/rest/v2.0/yop/platform/certs";
    private static final String CERT_DOWNLOAD_API_METHOD = "GET";
    private static final String CERT_DOWNLOAD_API_SECURITY = "YOP-SM2-SM3";
    private static final String CERT_DOWNLOAD_API_PARAM_SERIAL_NO = "serialNo";
    private static final String CERT_DOWNLOAD_API_PARAM_CERT_TYPE = "certType";

    // 证书缓存24小时
    private static final LoadingCache<String, List<X509Certificate>> PLATFORM_CERT_CACHE = initCache(24L, TimeUnit.HOURS);

    private static YopClient YOP_CLIENT;

    private static X509Certificate CFCA_ROOT_CERT, YOP_INTER_CERT, YOP_PLATFORM_RSA_CERT;
    private static final String QA_RSA_CERT_SERIAL_NO = "4032156487", PRO_RSA_CERT_SERIAL_NO = "4397139598";

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
        return YOP_INTER_CERT;
    }

    /**
     * 本地加载YOP平台RSA证书
     *
     * @return X509Certificate
     */
    public static X509Certificate getYopPlatformRsaCertFromLocal() {
        return YOP_PLATFORM_RSA_CERT;
    }

    /**
     * 加载平台证书
     *
     * @param appKey   应用标识
     * @param serialNo 证书序列号，可空
     * @return 最新平台证书
     */
    public static List<X509Certificate> loadPlatformSm2Certs(String appKey, String serialNo) {
        return loadPlatformSm2Certs(appKey, serialNo, null);
    }

    /**
     * 加载平台证书
     *
     * @param appKey     应用标识
     * @param serialNo   证书序列号，可空
     * @param serverRoot 平台证书请求端点，可空
     * @return 最新平台证书
     */
    public static List<X509Certificate> loadPlatformSm2Certs(String appKey, String serialNo, String serverRoot) {
        final String cacheKey = getCacheKey(appKey, serialNo, serverRoot);
        return loadPlatformSm2Certs(cacheKey);
    }

    private static List<X509Certificate> loadPlatformSm2Certs(String cacheKey) {
        try {
            final List<X509Certificate> cachedCerts = PLATFORM_CERT_CACHE.get(cacheKey);
            if (CollectionUtils.isNotEmpty(cachedCerts)) {
                return cachedCerts;
            }
            PLATFORM_CERT_CACHE.invalidate(cacheKey);
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
    public static List<X509Certificate> refreshPlatformSm2Certs(String appKey, String serialNo) {
        return refreshPlatformSm2Certs(appKey, serialNo, null);
    }

    /**
     * 加载并异步刷新平台证书
     *
     * @param appKey     应用标识
     * @param serialNo   证书序列号，可空
     * @param serverRoot 平台证书请求端点，可空
     * @return 最新平台证书
     */
    public static List<X509Certificate> refreshPlatformSm2Certs(String appKey, String serialNo, String serverRoot) {
        final String cacheKey = getCacheKey(appKey, serialNo, serverRoot);
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
    public static List<X509Certificate> reloadPlatformSm2Certs(String appKey, String serialNo) {
        return reloadPlatformSm2Certs(appKey, serialNo, null);
    }

    /**
     * 失效并重新加载平台证书
     *
     * @param appKey     应用标识
     * @param serialNo   证书序列号，可空
     * @param serverRoot 平台证书请求端点，可空
     * @return 最新平台证书
     */
    public static List<X509Certificate> reloadPlatformSm2Certs(String appKey, String serialNo, String serverRoot) {
        final String cacheKey = getCacheKey(appKey, serialNo, serverRoot);
        try {
            PLATFORM_CERT_CACHE.invalidate(cacheKey);
        } catch (Exception e) {
            LOGGER.warn("UnexpectedException occurred when invalidate platformCert for cacheKey:" + cacheKey, e);
        }
        return loadPlatformSm2Certs(cacheKey);
    }

    private static String getCacheKey(String appKey, String serialNo, String serverRoot) {
        return StringUtils.joinWith(COMMA, StringUtils.defaultIfBlank(appKey, YopConstants.YOP_DEFAULT_APPKEY),
                StringUtils.defaultIfBlank(serialNo, EMPTY), StringUtils.defaultIfBlank(serverRoot, EMPTY));
    }

    private static synchronized List<X509Certificate> doLoad(YopCredentials<?> yopCredentials, String serialNo, String serverRoot) {
        List<X509Certificate> result = Collections.emptyList();
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
            if (StringUtils.isNotBlank(serverRoot)) {
                request.getRequestConfig().setServerRoot(serverRoot);
            }
            request.addParameter(CERT_DOWNLOAD_API_PARAM_CERT_TYPE, CertTypeEnum.SM2.getValue());
            YopResponse response = YOP_CLIENT.request(request);

            // 响应解析
            List<YopPlatformPlainCert> platformCerts = parseYopResponse(response);

            // 证书验证
            List<X509Certificate> legalPlatformCerts = checkCerts(platformCerts);

            if (CollectionUtils.isNotEmpty(legalPlatformCerts)) {
                result = legalPlatformCerts;
            }
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
            return X509CertUtils.getX509Certificate(CertTypeEnum.SM2, certBytes);
        } catch (Exception e) {
            LOGGER.warn("fail to decode platform cert:" + platformCert + ", ex:", e);
        }
        return null;
    }

    private static X509Certificate verifyCert(X509Certificate platformCert) {
        try {
            X509CertUtils.verifyCertificate(CertTypeEnum.SM2, YOP_INTER_CERT.getPublicKey(), platformCert);
            return platformCert;
        } catch (Exception e) {
            LOGGER.error("error to verify platform cert:" + platformCert + ", ex:", e);
        }
        return null;
    }

    static {
        String cfcaRootFile = DEFAULT_CFCA_ROOT_FILE, yopInterFile = DEFAULT_YOP_INTER_FILE;
        String yopPlatformRsaCertSerialNo = PRO_RSA_CERT_SERIAL_NO;

        // 区分环境分别加载内置证书
        if (!EnvUtils.isProd()) {
            String env = EnvUtils.currentEnv(),
                    envPrefix = StringUtils.substringBefore(env, "_");
            cfcaRootFile = envPrefix + "_" + DEFAULT_CFCA_ROOT_FILE;
            yopInterFile = envPrefix + "_" + DEFAULT_YOP_INTER_FILE;
            yopPlatformRsaCertSerialNo = QA_RSA_CERT_SERIAL_NO;
        }

        try {
            // 根证书
            CFCA_ROOT_CERT = getX509Cert(DEFAULT_CERT_PATH + "/" + cfcaRootFile, CertTypeEnum.SM2);
            X509CertUtils.verifyCertificate(CertTypeEnum.SM2, null, CFCA_ROOT_CERT);

            // 中间证书
            YOP_INTER_CERT = getX509Cert(DEFAULT_CERT_PATH + "/" + yopInterFile, CertTypeEnum.SM2);
            X509CertUtils.verifyCertificate(CertTypeEnum.SM2, CFCA_ROOT_CERT.getPublicKey(), YOP_INTER_CERT);
        } catch (Exception e) {
            LOGGER.error("error when load sm2 parent certs, if you dont use sm2 just ignore it, ex:", e);
        }

        try {
            // YOP—RSA证书
            YOP_PLATFORM_RSA_CERT = getX509Cert(DEFAULT_CERT_PATH + "/" + YOP_RSA_PLATFORM_CERT_PREFIX +
                    yopPlatformRsaCertSerialNo + YOP_PLATFORM_CERT_POSTFIX, CertTypeEnum.RSA2048);
        } catch (Exception e) {
            LOGGER.warn("error when load yop rsa certs，if you dont use rsa just ignore it, ex:", e);
        }

        // 该内置client用于YOP通信，实时更新拉取平台证书
        YOP_CLIENT = YopGlobalClient.getClient();
    }

    private static X509Certificate getX509Cert(String certPath, CertTypeEnum certType) {
        final YopCertConfig yopCertConfig = new YopCertConfig();
        yopCertConfig.setCertType(certType);
        yopCertConfig.setValue(certPath);
        yopCertConfig.setStoreType(CertStoreType.FILE_CER);
        return ((YopPublicKey) YopCertParserFactory.getCertParser(YopCertCategory.PUBLIC, certType).parse(yopCertConfig)).getCert();
    }

    private static LoadingCache<String, List<X509Certificate>> initCache(Long expire, TimeUnit timeUnit) {
        CacheBuilder cacheBuilder = CacheBuilder.newBuilder();
        if (expire > 0) {
            cacheBuilder.expireAfterWrite(expire, timeUnit);
        }
        return cacheBuilder.build(new CacheLoader<String, List<X509Certificate>>() {
            @Override
            public List<X509Certificate> load(String cacheKey) throws Exception {
                LOGGER.debug("try to init platform cert for cacheKey:" + cacheKey);
                List<X509Certificate> platformCert = Collections.emptyList();
                try {
                    String[] split = StringUtils.splitPreserveAllTokens(cacheKey, COMMA);
                    String appKey = split[0], serialNo = split.length > 1 ? split[1] : null,
                            serverRoot = split.length > 2 ? split[2] : null;
                    platformCert = doLoad(YopCredentialsCache.get(appKey), serialNo, serverRoot);
                } catch (Exception ex) {
                    LOGGER.warn("UnexpectedException occurred when init platformCert for cacheKey:" + cacheKey, ex);
                }
                return platformCert;
            }
        });
    }

}
