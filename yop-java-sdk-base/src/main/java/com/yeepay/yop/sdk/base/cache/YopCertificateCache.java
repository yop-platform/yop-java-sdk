/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.cert.YopPlatformCertQueryResult;
import com.yeepay.yop.sdk.model.cert.YopPlatformPlainCert;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.security.cert.YopPublicKey;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.ClientUtils;
import com.yeepay.yop.sdk.utils.EnvUtils;
import com.yeepay.yop.sdk.utils.JsonUtils;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.*;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COMMA;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;
import static com.yeepay.yop.sdk.utils.X509CertUtils.getLocalCertDirs;

/**
 * title: Yop证书缓存类<br>
 * description: SM2证书链缓存+SM2证书分发缓存+本地rsa证书缓存<br>
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
    private static final LoadingCache<String, List<X509Certificate>> PLATFORM_CERT_CACHE
            = initCache(24L, TimeUnit.HOURS);

    // <{provider}:{env},X509Certificate>
    private static final Map<String, X509Certificate> CFCA_ROOT_CERT_MAP = Maps.newConcurrentMap();
    private static final Map<String, X509Certificate> YOP_INTER_CERT_MAP = Maps.newConcurrentMap();
    private static final Map<String, X509Certificate> YOP_PLATFORM_RSA_CERT_MAP = Maps.newConcurrentMap();
    private static final Map<String, PublicKey> YOP_PLATFORM_RSA_KEY_MAP = Maps.newConcurrentMap();

    /**
     * 本地加载cfca根证书
     *
     * @return X509Certificate
     */
    public static X509Certificate getCfcaRootCertFromLocal() {
        return getCfcaRootCertFromLocal(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, YOP_DEFAULT_APPKEY);
    }
    public static X509Certificate getCfcaRootCertFromLocal(String provider, String env, String appKey) {
        return CFCA_ROOT_CERT_MAP.computeIfAbsent(localKeyId(provider, env, appKey, ""),
                p -> loadRootCertFromLocal(provider, env, appKey));
    }

    private static X509Certificate loadRootCertFromLocal(String provider, String env, String appKey) {
        try {
            // 根证书
            final X509Certificate cfcaRootCert = loadPlatformCertFromLocal(provider, env, appKey,
                    DEFAULT_CFCA_ROOT_FILE, CertTypeEnum.SM2);
            X509CertUtils.verifyCertificate(provider, env, CertTypeEnum.SM2, null, cfcaRootCert);
            return cfcaRootCert;
        } catch (YopClientException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("error when load sm2 root cert, ex:", e);
        }
        throw new YopClientException("Config Error, platform sm2 root cert not found, provider:"
                + provider + ", env:" + env + ", appKey:" + appKey);
    }

    private static String localKeyId(String provider, String env, String appKey, String serialNo) {
        return StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER) + COMMA
                + StringUtils.defaultString(env, YOP_DEFAULT_ENV) + COMMA + appKey + COMMA + serialNo;
    }

    /**
     * 本地加载YOP中间证书
     *
     * @return X509Certificate
     */
    public static X509Certificate getYopInterCertFromLocal() {
        return getYopInterCertFromLocal(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, YOP_DEFAULT_APPKEY);
    }

    public static X509Certificate getYopInterCertFromLocal(String provider, String env, String appKey) {
        return YOP_INTER_CERT_MAP.computeIfAbsent(localKeyId(provider, env, appKey, ""),
                p -> loadInterCertFromLocal(provider, env, appKey));
    }

    private static X509Certificate loadInterCertFromLocal(String provider, String env, String appKey) {
        try {
            // 中间证书
            final X509Certificate yopInterCert = loadPlatformCertFromLocal(provider, env, appKey,
                    DEFAULT_YOP_INTER_FILE, CertTypeEnum.SM2);
            // 校验中间证书
            X509CertUtils.verifyCertificate(provider, env, CertTypeEnum.SM2,
                    getCfcaRootCertFromLocal(provider, env, appKey).getPublicKey(), yopInterCert);
            return yopInterCert;
        } catch (YopClientException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("error when load sm2 inter certs, ex:", e);
        }
        throw new YopClientException("Config Error, platform sm2 inter cert not found, provider:"
                + provider + ", env:" + env + ", appKey:" + appKey);
    }

    private static X509Certificate loadPlatformCertFromLocal(String provider, String env, String appKey,
                                                             String certFile, CertTypeEnum certType) {
        Set<String> certDirsOrdered = getLocalCertDirs(DEFAULT_CERT_PATH, provider, env, appKey);
        for (String certDir : certDirsOrdered) {
            X509Certificate cert = doLoadPlatformCertFromLocal(certDir + "/" + certFile, certType);
            if (null != cert) {
                return cert;
            }
        }
        throw new YopClientException("Config Error, platform cert not found, provider:"
                + provider + ", env:" + env + ", appKey:" + appKey + ", certType:" + certType + ", certFile:" + certFile);
    }

    /**
     * 本地加载YOP平台RSA证书
     *
     * @return X509Certificate
     */
    public static X509Certificate getYopPlatformRsaCertFromLocal() {
        return getYopPlatformRsaCertFromLocal(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, YOP_DEFAULT_APPKEY,
                YOP_RSA_PLATFORM_CERT_DEFAULT_SERIAL_NO);
    }

    public static X509Certificate getYopPlatformRsaCertFromLocal(String provider, String env,
                                                                 String appKey, String serialNo) {
        return YOP_PLATFORM_RSA_CERT_MAP.computeIfAbsent(localKeyId(provider, env, appKey, serialNo),
                p -> loadPlatformCertFromLocal(provider, env, appKey, YOP_RSA_PLATFORM_CERT_PREFIX +
                        serialNo + YOP_PLATFORM_CERT_POSTFIX, CertTypeEnum.RSA2048));
    }

    public static PublicKey getYopPlatformRsaKeyFromLocal(String provider, String env,
                                                          String appKey, String serialNo) {
        return YOP_PLATFORM_RSA_KEY_MAP.computeIfAbsent(localKeyId(provider, env, appKey, serialNo),
                p -> loadPlatformRsaKeyFromLocal(provider, env, appKey, serialNo));
    }

    private static X509Certificate doLoadPlatformCertFromLocal(String certPath, CertTypeEnum certType) {
        try {
            return getX509Cert(certPath, certType);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("local platform cert not found, certType:{}, certPath:{}, msg:{}",
                        certType, certPath, ExceptionUtils.getMessage(e));
            }
        }
        return null;
    }

    private static PublicKey loadPlatformRsaKeyFromLocal(String provider, String env,
                                                         String appKey, String serialNo) {
        try {
            // YOP—RSA公钥配置
            // 兼容yeepay特有的旧逻辑
            if (EnvUtils.isOldSetting(provider, env, appKey)) {
                provider = PROVIDER_YEEPAY;
                env = ENV_QA;
            }

            // 优先读取用户配置
            final YopSdkConfig sdkConfig = ClientUtils.getCurrentSdkConfigProvider().getConfig(provider, env);
            final YopCertConfig[] yopPublicKey = sdkConfig.getYopPublicKey();
            final PublicKey publicKeyFound = choosePlatformRsaKeyFromLocal(yopPublicKey);
            if (null != publicKeyFound) {
                return publicKeyFound;
            }

            // 兜底读取内置证书
            return loadPlatformCertFromLocal(provider, env, appKey, YOP_RSA_PLATFORM_CERT_PREFIX +
                    serialNo + YOP_PLATFORM_CERT_POSTFIX, CertTypeEnum.RSA2048).getPublicKey();
        } catch (YopClientException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.warn("error when load yop rsa certs, ex:", e);
        }
        throw new YopClientException("Config Error, platform rsa certs not found, provider:"
                + provider + ", env:" + env + ", appKey:" + appKey + ", serialNo:" + serialNo);
    }

    private static PublicKey choosePlatformRsaKeyFromLocal(YopCertConfig[] yopPublicKey) {
        if (null != yopPublicKey && yopPublicKey.length > 0) {
            for (int i = 0; i < yopPublicKey.length; i++) {
                YopCertConfig certConfig = yopPublicKey[i];
                try {
                    if (!CertTypeEnum.RSA2048.equals(certConfig.getCertType())) {
                        continue;
                    }
                    return ((YopPublicKey) YopCertParserFactory.getCertParser(YopCertCategory.PUBLIC,
                            CertTypeEnum.RSA2048).parse(certConfig)).getPublicKey();
                } catch (Exception e) {
                    LOGGER.warn("Config Error, yopPublicKey:{}", certConfig);
                }
            }
        }
        return null;
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
        return loadPlatformSm2Certs(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, serialNo, serverRoot);
    }

    public static List<X509Certificate> loadPlatformSm2Certs(String provider, String env, String appKey, String serialNo, String serverRoot) {
        final String cacheKey = getCacheKey(provider, env, appKey, serialNo, serverRoot);
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
        return refreshPlatformSm2Certs(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, serialNo, serverRoot);
    }

    public static List<X509Certificate> refreshPlatformSm2Certs(String provider, String env, String appKey, String serialNo, String serverRoot) {
        final String cacheKey = getCacheKey(provider, env, appKey, serialNo, serverRoot);
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
        return reloadPlatformSm2Certs(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV, appKey, serialNo, serverRoot);
    }

    public static List<X509Certificate> reloadPlatformSm2Certs(String provider, String env, String appKey, String serialNo, String serverRoot) {
        final String cacheKey = getCacheKey(provider, env, appKey, serialNo, serverRoot);
        try {
            PLATFORM_CERT_CACHE.invalidate(cacheKey);
        } catch (Exception e) {
            LOGGER.warn("UnexpectedException occurred when invalidate platformCert for cacheKey:" + cacheKey, e);
        }
        return loadPlatformSm2Certs(cacheKey);
    }

    private static String getCacheKey(String provider, String env, String appKey, String serialNo, String serverRoot) {
        return StringUtils.joinWith(COMMA, StringUtils.defaultIfBlank(provider, YOP_DEFAULT_PROVIDER),
                StringUtils.defaultIfBlank(env, YOP_DEFAULT_ENV),
                StringUtils.defaultIfBlank(appKey, YopConstants.YOP_DEFAULT_APPKEY),
                StringUtils.defaultIfBlank(serialNo, EMPTY), StringUtils.defaultIfBlank(serverRoot, EMPTY));
    }

    private static synchronized List<X509Certificate> doLoad(String provider, String env, String appKey,
                                                             String serialNo, String serverRoot) {
        List<X509Certificate> result = Collections.emptyList();
        try {
            final YopCredentials<?> yopCredentials = YopCredentialsCache.get(provider, env, appKey);
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
            YopResponse response = ClientUtils.getAvailableYopClient(provider, env).request(request);

            // 响应解析
            List<YopPlatformPlainCert> platformCerts = parseYopResponse(response);

            // 证书验证
            List<X509Certificate> legalPlatformCerts = checkCerts(provider, env, appKey, platformCerts);

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

    private static List<X509Certificate> checkCerts(String provider, String env, String appKey,
                                                    List<YopPlatformPlainCert> platformCerts) {
        if (CollectionUtils.isNotEmpty(platformCerts)) {
            List<X509Certificate> result = Lists.newArrayList();
            for (YopPlatformPlainCert platformCert : platformCerts) {
                X509Certificate tmp = decodeCert(platformCert);
                if (null != tmp) {
                    tmp = verifyCert(provider, env, appKey, tmp);
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

    private static X509Certificate verifyCert(String provider, String env, String appKey,
                                              X509Certificate platformCert) {
        try {
            X509CertUtils.verifyCertificate(provider, env, CertTypeEnum.SM2,
                    getYopInterCertFromLocal(provider, env, appKey).getPublicKey(), platformCert);
            return platformCert;
        } catch (Exception e) {
            LOGGER.error("error to verify platform cert:" + platformCert + ", ex:", e);
        }
        return null;
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
                    String provider = split[0], env = split[1], appKey = split[2], serialNo = split.length > 3 ? split[3] : null,
                            serverRoot = split.length > 4 ? split[4] : null;
                    platformCert = doLoad(provider, env, appKey, serialNo, serverRoot);
                } catch (Exception ex) {
                    LOGGER.warn("UnexpectedException occurred when init platformCert for cacheKey:" + cacheKey, ex);
                }
                return platformCert;
            }
        });
    }

}
