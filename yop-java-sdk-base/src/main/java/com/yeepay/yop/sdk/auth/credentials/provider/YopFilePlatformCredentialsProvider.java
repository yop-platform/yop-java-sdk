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
import com.yeepay.yop.sdk.crypto.YopCertCategory;
import com.yeepay.yop.sdk.crypto.YopCertParserFactory;
import com.yeepay.yop.sdk.crypto.YopPublicKey;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.StreamUtils;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.security.cert.X509Certificate;
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
        Map<String, X509Certificate> localCerts = loadAndVerify(yopCertStore, serialNo);

        // 从内置路径加载
        if (MapUtils.isEmpty(localCerts) || !localCerts.containsKey(serialNo)) {
            localCerts = loadAndVerify(YopConstants.DEFAULT_LOCAL_YOP_CERT_STORE, serialNo);
        }

        if (MapUtils.isNotEmpty(localCerts)) {
            if (localCerts.containsKey(serialNo)) {
                return toCredentials(CertTypeEnum.SM2, localCerts.get(serialNo));
            }
        }
        LOGGER.debug("no available platform cert from store, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);
        return null;
    }

    @Override
    public void saveCertsIntoStore(Map<String, X509Certificate> plainCerts) {
        saveCertsIntoStore(YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore(), plainCerts);
    }

    private void saveCertsIntoStore(YopCertStore yopCertStore, Map<String, X509Certificate> plainCerts) {
        if (MapUtils.isEmpty(plainCerts)) {
            return;
        }

        // 存储指定目录
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

    private Map<String, X509Certificate> loadAndVerify(YopCertStore yopCertStore, String serialNo) {
        LOGGER.debug("load sm2 cert from local, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);
        Map<String, X509Certificate> certMap = Maps.newHashMap();
        if (StringUtils.isNotBlank(yopCertStore.getPath()) && BooleanUtils.isTrue(yopCertStore.getEnable())) {
            try {
                String filename = yopCertStore.getPath() + "/" + YOP_SM_PLATFORM_CERT_PREFIX + serialNo + YOP_PLATFORM_CERT_POSTFIX;
                final YopCertConfig yopCertConfig = new YopCertConfig();
                yopCertConfig.setCertType(CertTypeEnum.SM2);
                yopCertConfig.setValue(filename);
                yopCertConfig.setStoreType(CertStoreType.FILE_CER);
                final X509Certificate cert =
                        ((YopPublicKey) YopCertParserFactory.getCertParser(YopCertCategory.PUBLIC, CertTypeEnum.SM2).parse(yopCertConfig)).getCert();
                String realSerialNo = cert.getSerialNumber().toString();
                X509CertUtils.verifyCertificate(CertTypeEnum.SM2, YopCertificateCache.getYopInterCertFromLocal().getPublicKey(), cert);
                if (!realSerialNo.equals(serialNo)) {
                    LOGGER.warn("wrong file name for cert, serialNo:{}, realSerialNo:{}", serialNo, realSerialNo);
                    certMap.put(serialNo, cert);
                }
                certMap.put(realSerialNo, cert);
            } catch (Exception e) {
                LOGGER.error("error when load cert from local file, serialNo:" + serialNo + ", ex:", e);
            }
        }
        return certMap;
    }
}
