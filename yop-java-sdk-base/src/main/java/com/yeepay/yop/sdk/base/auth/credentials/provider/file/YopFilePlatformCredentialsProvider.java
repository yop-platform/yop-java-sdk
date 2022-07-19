/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.credentials.provider.file;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.base.auth.credentials.provider.YopBasePlatformCredentialsProvider;
import com.yeepay.yop.sdk.base.cache.YopCertificateCache;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.base.security.cert.X509CertSupportFactory;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParserFactory;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.security.cert.YopPublicKey;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yeepay.yop.sdk.YopConstants.YOP_PLATFORM_CERT_POSTFIX;
import static com.yeepay.yop.sdk.YopConstants.YOP_SM_PLATFORM_CERT_PREFIX;

/**
 * title: 基于文件的平台证书提供方(默认实现)<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:30 PM
 */
public class YopFilePlatformCredentialsProvider extends YopBasePlatformCredentialsProvider {

    protected static final Logger LOGGER = LoggerFactory.getLogger(YopFilePlatformCredentialsProvider.class);

    protected static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(200),
            new ThreadFactoryBuilder().setNameFormat("yop-platform-cert-store-task-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    protected YopPlatformCredentials loadCredentialsFromStore(String appKey, String serialNo) {
        // 从指定目录加载
        YopCertStore yopCertStore = YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore();
        Map<String, X509Certificate> localCerts = loadAndVerify(yopCertStore, serialNo, true);

        // 从内置路径加载
        if (MapUtils.isEmpty(localCerts) || !localCerts.containsKey(serialNo)) {
            localCerts = loadAndVerify(YopConstants.DEFAULT_LOCAL_YOP_CERT_STORE, serialNo, false);
        }

        if (MapUtils.isNotEmpty(localCerts)) {
            if (localCerts.containsKey(serialNo)) {
                return convertCredentials(appKey, CertTypeEnum.SM2.name(), localCerts.get(serialNo));
            }
        }
        LOGGER.debug("no available platform cert from store, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);
        return null;
    }

    @Override
    public YopPlatformCredentials storeCredentials(String appKey, String credentialType, X509Certificate cert) {
        return doStore(appKey, credentialType, cert, YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore());
    }

    private YopPlatformCredentials doStore(String appKey, String credentialType, X509Certificate cert, YopCertStore yopCertStore) {
        YopPlatformCredentials result = convertCredentials(appKey, credentialType, cert);

        // 默认仅放内存，商户可配置存放磁盘
        if (null == yopCertStore || !BooleanUtils.isTrue(yopCertStore.getEnable())) {
            return result;
        }

        // 异步存入本地
        THREAD_POOL.submit(() -> {
            try {
                final File certStoreDir = createStoreDirIfNecessary(yopCertStore);
                if (null != certStoreDir) {
                    writeCertToFileStore(certStoreDir, cert);
                }
            } catch (Exception e) {
                LOGGER.warn("error when X509Certificate, ex:", e);
            }
        });

        return result;
    }

    private void writeCertToFileStore(File certStoreDir, X509Certificate cert) {
        try {
            final String serialNo = X509CertUtils.parseToHex(cert.getSerialNumber().toString());
            final File certFile = new File(certStoreDir, YOP_SM_PLATFORM_CERT_PREFIX + serialNo + YOP_PLATFORM_CERT_POSTFIX);
            X509CertSupportFactory.getSupport(CertTypeEnum.SM2.name()).writeToFile(cert, certFile);
        } catch (Exception e) {
            LOGGER.error("error when write yop cert to file, ex:", e);
        }
    }

    private File createStoreDirIfNecessary(YopCertStore yopCertStore) {
        try {
            File certStoreDir = new File(yopCertStore.getPath());
            if (!certStoreDir.exists() && !certStoreDir.mkdirs()) {
                LOGGER.warn("fail when create yop cert store dir, {}", yopCertStore);
            } else {
                return certStoreDir;
            }
        } catch (Exception e) {
            LOGGER.error("error when create yop cert store dir, ex:", e);
        }
        return null;
    }

    private Map<String, X509Certificate> loadAndVerify(YopCertStore yopCertStore, String serialNo, boolean absolutePath) {
        LOGGER.debug("begin load sm2 cert from local, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);
        Map<String, X509Certificate> certMap = Maps.newHashMap();
        if (StringUtils.isNotBlank(yopCertStore.getPath()) && BooleanUtils.isTrue(yopCertStore.getEnable())) {
            try {
                String filename = yopCertStore.getPath() + "/" + YOP_SM_PLATFORM_CERT_PREFIX + serialNo + YOP_PLATFORM_CERT_POSTFIX;
                if (absolutePath && !new File(filename).exists()) {
                    LOGGER.warn("wrong file path for sm2 cert, serialNo:{}, path:{}", serialNo, filename);
                    return certMap;
                }
                final YopCertConfig yopCertConfig = new YopCertConfig();
                yopCertConfig.setCertType(CertTypeEnum.SM2);
                yopCertConfig.setValue(filename);
                yopCertConfig.setStoreType(CertStoreType.FILE_CER);
                final X509Certificate cert =
                        ((YopPublicKey) YopCertParserFactory.getCertParser(YopCertCategory.PUBLIC, CertTypeEnum.SM2).parse(yopCertConfig)).getCert();
                String realSerialNo = X509CertUtils.parseToHex(cert.getSerialNumber().toString());
                X509CertUtils.verifyCertificate(CertTypeEnum.SM2, YopCertificateCache.getYopInterCertFromLocal().getPublicKey(), cert);
                if (!realSerialNo.equals(serialNo)) {
                    LOGGER.warn("wrong file name for sm2 cert, serialNo:{}, realSerialNo:{}", serialNo, realSerialNo);
                    certMap.put(serialNo, cert);
                }
                certMap.put(realSerialNo, cert);
            } catch (Exception e) {
                LOGGER.error("error when load sm2 cert from local file, serialNo:" + serialNo + ", ex:", e);
            }
        }
        return certMap;
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
}
