/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.*;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopFilePlatformCredentialsProvider.class);

    private static final String CERT_DOWNLOAD_API_URI = "";
    private static final String CERT_DOWNLOAD_API_METHOD = "POST";
    private static X509Certificate cfcaRoot, yopInter;
    {
        try {
            cfcaRoot = Sm2CertUtils.getX509Certificate(FileUtils.getResourceAsStream("config/certs/cfca_root.cer"));
            try {
                cfcaRoot.checkValidity();
            } catch (Exception e) {
                throw new YopClientException("invalid cfca root cert");
            }
            yopInter = Sm2CertUtils.getX509Certificate(FileUtils.getResourceAsStream("config/certs/yop_inter.cer"));
            if (!Sm2CertUtils.verifyCertificate((BCECPublicKey) cfcaRoot.getPublicKey(), yopInter)) {
                throw new YopClientException("invalid yop inter cert");
            }
        } catch (Exception e) {
            LOGGER.error("error when load parent certs, ex:", e);
        }
    }
    private Map<String, YopPlatformCredentials> credentialsMap = new ConcurrentHashMap<>();


    @Override
    public YopPlatformCredentials getCredentials(String appKey, String serialNo) {
        serialNo = StringUtils.defaultIfBlank(serialNo, YOP_CERT_RSA_DEFAULT_SERIALNO);
        YopPlatformCredentials foundCredentials = credentialsMap.get(serialNo);
        if (Objects.isNull(foundCredentials)) {
            reload(appKey);
            foundCredentials = credentialsMap.get(serialNo);
        }
        return foundCredentials;
    }

    @Override
    public void reload(String appKey) {
        YopSdkConfigProvider sdkConfigProvider = YopSdkConfigProviderRegistry.getProvider();
        final YopSdkConfig sdkConfig = sdkConfigProvider.getConfig();

        // 1.加载RSA公钥
        final PublicKey rsaPublicKey = sdkConfig.loadYopPublicKey(CertTypeEnum.RSA2048);
        if (null != rsaPublicKey) {
            credentialsMap.put(YOP_CERT_RSA_DEFAULT_SERIALNO, new YopPlatformCredentialsHolder()
                    .withSerialNo(YOP_CERT_RSA_DEFAULT_SERIALNO).withPublicKey(CertTypeEnum.RSA2048, rsaPublicKey));
        }

        // 2.加载SM2证书
        YopCertStore yopCertStore = sdkConfig.getYopCertStore();
        Map<String, X509Certificate> certMap = new LinkedHashMap<>();

        // 2.1本地加载
        loadAndVerifyFromLocal(yopCertStore, certMap);

        // 2.2远端加载
        Map<String, X509Certificate> remoteCertMap = loadAndVerifyFromRemote(appKey, sdkConfig.getYopEncryptKey());

        // 2.3合并两端
        if (MapUtils.isNotEmpty(remoteCertMap)) {
            certMap.putAll(remoteCertMap);
        }

        // 3.刷新内存
        if (MapUtils.isNotEmpty(certMap)) {
            certMap.forEach((serialNo, cert) -> credentialsMap.put(serialNo, new YopPlatformCredentialsHolder()
                    .withSerialNo(serialNo).withPublicKey(CertTypeEnum.SM2, cert.getPublicKey())));
        } else {
            LOGGER.warn("no available sm2 cert from local and remote");
        }

        // 4.保存文件 todo 异步
        if (MapUtils.isNotEmpty(remoteCertMap)) {
            storeCerts(yopCertStore, remoteCertMap);
        }
    }

    private Map<String, X509Certificate> loadAndVerifyFromRemote(String appKey, YopCertConfig[] yopEncryptKey) {
        try {
            YopClient yopClient = YopClientBuilder.builder().build();
            YopRequest request = new YopRequest(CERT_DOWNLOAD_API_URI, CERT_DOWNLOAD_API_METHOD);
            // 指定appKey
            if (StringUtils.isNotBlank(appKey) && !StringUtils.equals("default", appKey)) {
                request.getRequestConfig().setAppKey(appKey);
            }
            // 不强制验签
            request.getRequestConfig().setForceVerifySign(false);
            YopResponse response = yopClient.request(request);

            // 响应解析
            List<EncryptCertificate> encryptCerts = parseYopResponse(response);

            // 证书解密
            return decryptCerts(encryptCerts, yopEncryptKey);
        } catch (Exception e) {
            LOGGER.error("error when load sm2 cert from remote, ex:", e);
        }
        return null;
    }

    private void loadAndVerifyFromLocal(YopCertStore yopCertStore, Map<String, X509Certificate> certMap) {
        if (StringUtils.isNotBlank(yopCertStore.getPath())) {
            File certStoreDir = new File(yopCertStore.getPath());
            if (certStoreDir.exists() && certStoreDir.isDirectory()) {
                File[] certFiles = certStoreDir.listFiles();
                if (ArrayUtils.isNotEmpty(certFiles)) {
                    for (File certFile : certFiles) {
                        try {
                            final X509Certificate cert = Sm2CertUtils.getX509Certificate(new FileInputStream(certFile));
                            cert.checkValidity();
                            if (Sm2CertUtils.verifyCertificate((BCECPublicKey) yopInter.getPublicKey(), cert)) {
                                certMap.put(cert.getSerialNumber().toString(), cert);
                            } else {
                                LOGGER.warn("invalid cert from local file:{}", certFile.getName());
                            }
                        } catch (Exception e) {
                            LOGGER.error("error when load cert from local file:" + certFile.getName() + ", ex:", e);
                        }
                    }
                }
            }
        }

    }

    private void storeCerts(YopCertStore yopCertStore, Map<String, X509Certificate> plainCerts) {
        try {
            if (yopCertStore.getEnable()) {
                final File certStoreDir = new File(yopCertStore.getPath());
                if (!certStoreDir.exists()) {
                    certStoreDir.mkdir();
                }
                // todo 对比内容, 中断时有写一半的情况？？
                for (Map.Entry<String, X509Certificate> certificateEntry : plainCerts.entrySet()) {
                    final File certFile = new File(certStoreDir, "yop_cert_" + certificateEntry.getKey() + ".cer");
                    try (FileWriter fileWriter = new FileWriter(certFile)){
                        fileWriter.write(Encodes.encodeBase64(certificateEntry.getValue().getEncoded()));
                        fileWriter.flush();
                    }
                }
            } else {
                LOGGER.info("yop cert store not enable.");
            }
        } catch (Exception e) {
            LOGGER.error("error when store yop cert, ex:", e);
        }
    }

    private Map<String, X509Certificate> decryptCerts(List<EncryptCertificate> encryptCerts, YopCertConfig[] yopEncryptKey) {
        if (CollectionUtils.isNotEmpty(encryptCerts)) {
            Map<String, X509Certificate> certMap = Maps.newHashMapWithExpectedSize(encryptCerts.size());
            for (EncryptCertificate encryptCert : encryptCerts) {
                X509Certificate decryptCert = decryptCert(encryptCert, yopEncryptKey);
                if (null != decryptCert) {
                    certMap.put(decryptCert.getSerialNumber().toString(), decryptCert);
                }
            }
            return certMap;
        }
        return null;
    }

    private X509Certificate decryptCert(EncryptCertificate encryptCert, YopCertConfig[] yopEncryptKey) {
        for (YopCertConfig yopCertkey : yopEncryptKey) {
            if (yopCertkey.getCertType() == CertTypeEnum.SM2) {
                byte[] certBytes = null;
                final String certKeyHex = yopCertkey.getValue();
                try {
                    certBytes = Sm4Utils.decrypt_GCM_NoPadding(Encodes.decodeHex(certKeyHex),
                            encryptCert.getAssociatedData(), encryptCert.getNonce(), encryptCert.getCiphertext());
                } catch (Exception e) {
                    LOGGER.warn("fail to try decrypt cert, certKey:" + certKeyHex + ", cert:" + encryptCert + ", ex:", e);
                }
                if (null != certBytes) {
                    try {
                        return Sm2CertUtils.getX509Certificate(certBytes);
                    } catch (Exception e) {
                        LOGGER.error("error to parse cert bytes, certKey:" + certKeyHex + ", cert:" + encryptCert + ", ex:", e);
                    }
                }
            }
        }
        return null;
    }

    private List<EncryptCertificate> parseYopResponse(YopResponse response) {
        List<EncryptCertificate> encryptCerts = new ArrayList<>();
        List<Map> result = (List<Map>) response.getResult();
        if (CollectionUtils.isNotEmpty(result)) {
            // todo 对接网关接口
        }
        return encryptCerts;
    }
}
