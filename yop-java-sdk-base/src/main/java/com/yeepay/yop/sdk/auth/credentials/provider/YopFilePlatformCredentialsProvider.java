/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.cache.YopCertificateCache;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.crypto.X509CertSupportFactory;
import com.yeepay.yop.sdk.crypto.YopCertCategory;
import com.yeepay.yop.sdk.crypto.YopCertParserFactory;
import com.yeepay.yop.sdk.crypto.YopPublicKey;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

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
                return convertToCredentials(appKey, CertTypeEnum.SM2, localCerts.get(serialNo));
            }
        }
        LOGGER.debug("no available platform cert from store, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);
        return null;
    }

    @Override
    public void saveCertsIntoStore(String appKey, List<X509Certificate> certificates) {
        saveCertsIntoStore(YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore(), certificates);
    }

    private void saveCertsIntoStore(YopCertStore yopCertStore, List<X509Certificate> certificates) {
        if (CollectionUtils.isEmpty(certificates)) {
            return;
        }

        // 存储指定目录
        if (null == yopCertStore || !BooleanUtils.isTrue(yopCertStore.getEnable())) {
            return;
        }
        for (X509Certificate certificate : certificates) {
            try {
                final File certStoreDir = new File(yopCertStore.getPath());
                if (!certStoreDir.exists()) {
                    certStoreDir.mkdirs();
                }
                final String serialNo = certificate.getSerialNumber().toString();
                final File certFile = new File(certStoreDir, YOP_SM_PLATFORM_CERT_PREFIX + serialNo + YOP_PLATFORM_CERT_POSTFIX);
                X509CertSupportFactory.getSupport(CertTypeEnum.SM2.name()).writeToFile(certificate, certFile);
            } catch (Exception e) {
                LOGGER.error("error when store yop cert, ex:", e);
            }
        }
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
                String realSerialNo = cert.getSerialNumber().toString();
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
}
