/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider.loader;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.auth.credentials.provider.EncryptCertificate;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.Sm2CertUtils;
import com.yeepay.yop.sdk.utils.Sm4Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
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
 * @since 2021-02-25
 */
public class YopSm2PlatformCredentialsLoader implements YopPlatformCredentialsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSm2PlatformCredentialsLoader.class);

    private static final String CERT_DOWNLOAD_API_URI = "/rest/v1.0/yop/platform/certs";
    private static final String CERT_DOWNLOAD_API_METHOD = "GET";
    private static final String CERT_DOWNLOAD_API_SECURITY = "YOP-SM2-SM3";

    private Map<String, YopPlatformCredentials> credentialsMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, YopPlatformCredentials> load(String appKey, String serialNo) {
        if (!credentialsMap.containsKey(serialNo)) {
            reload(appKey, serialNo);
        }
        return Collections.unmodifiableMap(credentialsMap);
    }

    @Override
    public synchronized Map<String, YopPlatformCredentials> reload(String appKey, String serialNo) {
        Map<String, X509Certificate> x509CertificateMap = loadAndVerifyFromRemote(appKey, serialNo, YopCredentialsProviderRegistry.getProvider().getIsvEncryptKey(appKey));
        if (MapUtils.isNotEmpty(x509CertificateMap)) {
            credentialsMap.putAll(storeCerts(YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore(), x509CertificateMap));
        }
        return Collections.unmodifiableMap(credentialsMap);
    }

    private Map<String, YopPlatformCredentials> storeCerts(YopCertStore yopCertStore, Map<String, X509Certificate> plainCerts) {
        Map<String, YopPlatformCredentials> result = new LinkedHashMap<>();
        for (Map.Entry<String, X509Certificate> certificateEntry : plainCerts.entrySet()) {
            try {
                result.put(certificateEntry.getKey(), new YopPlatformCredentialsHolder()
                        .withSerialNo(certificateEntry.getKey()).withPublicKey(CertTypeEnum.SM2, certificateEntry.getValue().getPublicKey()));
                if (yopCertStore.getEnable()) {
                    final File certStoreDir = new File(yopCertStore.getPath());
                    if (!certStoreDir.exists()) {
                        certStoreDir.mkdirs();
                    }
                    final File certFile = new File(certStoreDir, "yop_cert_" + certificateEntry.getKey() + ".pem");
                    try (JcaPEMWriter jcaPEMWriter = new JcaPEMWriter(new FileWriter(certFile))) {
                        jcaPEMWriter.writeObject(new PemObject("CERTIFICATE", certificateEntry.getValue().getEncoded()));
                    }
                }
            } catch (Exception e) {
                LOGGER.error("error when store yop cert, ex:", e);
            }
        }
        return result;
    }

    private Map<String, X509Certificate> loadAndVerifyFromRemote(String appKey, String serialNo, YopCertConfig[] isvEncryptKey) {
        try {
            YopClient yopClient = YopClientBuilder.builder().build();
            YopRequest request = new YopRequest(CERT_DOWNLOAD_API_URI, CERT_DOWNLOAD_API_METHOD);
            // 指定appKey
            if (StringUtils.isNotBlank(appKey) && !StringUtils.equals("default", appKey)) {
                request.getRequestConfig().setAppKey(appKey);
            }
            // 跳过验签
            request.getRequestConfig().setSkipVerifySign(true);
            request.getRequestConfig().setSecurityReq(CERT_DOWNLOAD_API_SECURITY);
            if (StringUtils.isNotBlank(serialNo)) {
                request.addParameter("serialNo", serialNo);
            }
            YopResponse response = yopClient.request(request);

            // 响应解析
            List<EncryptCertificate> encryptCerts = parseYopResponse(response);

            // 证书解密
            return decryptCerts(encryptCerts, isvEncryptKey);
        } catch (Exception e) {
            LOGGER.error("error when load sm2 cert from remote, ex:", e);
        }
        return null;
    }

    private Map<String, X509Certificate> decryptCerts(List<EncryptCertificate> encryptCerts, YopCertConfig[] isvEncryptKey) {
        if (CollectionUtils.isNotEmpty(encryptCerts)) {
            Map<String, X509Certificate> certMap = Maps.newHashMapWithExpectedSize(encryptCerts.size());
            for (EncryptCertificate encryptCert : encryptCerts) {
                X509Certificate decryptCert = decryptCert(encryptCert, isvEncryptKey);
                if (null != decryptCert) {
                    certMap.put(decryptCert.getSerialNumber().toString(), decryptCert);
                }
            }
            return certMap;
        }
        return null;
    }

    private X509Certificate decryptCert(EncryptCertificate encryptCert, YopCertConfig[] isvEncryptKey) {
        for (YopCertConfig yopCertkey : isvEncryptKey) {
            if (yopCertkey.getCertType() == CertTypeEnum.SM4) {
                byte[] certBytes = null;
                final String certKeyBase64 = yopCertkey.getValue();
                try {
                    certBytes = Sm4Utils.decrypt_GCM_NoPadding(Encodes.decodeBase64(certKeyBase64),
                            encryptCert.getAssociatedData(), encryptCert.getNonce(), encryptCert.getCiphertext());
                } catch (Exception e) {
                    LOGGER.warn("fail to try decrypt cert, certKey:" + certKeyBase64 + ", cert:" + encryptCert + ", ex:", e);
                }
                if (null != certBytes) {
                    try {
                        return Sm2CertUtils.getX509Certificate(certBytes);
                    } catch (Exception e) {
                        LOGGER.error("error to parse cert bytes, certKey:" + certKeyBase64 + ", cert:" + encryptCert + ", ex:", e);
                    }
                }
            } else {
                LOGGER.warn("no available sm4 isv_encrypt_key found!");
            }
        }
        return null;
    }

    private List<EncryptCertificate> parseYopResponse(YopResponse response) {
        List<EncryptCertificate> encryptCerts = new ArrayList<>();
        Map result = (Map) response.getResult();
        if (MapUtils.isNotEmpty(result)) {
            List<Map> data = (List<Map>) result.get("data");
            if (CollectionUtils.isNotEmpty(data)) {
                for (Map map : data) {
                    Map encryptCertificate = (Map) map.get("encryptCertificate");
                    if (null != encryptCertificate) {
                        String algorithm = (String) encryptCertificate.get("algorithm");
                        String nonce = (String) encryptCertificate.get("nonce");
                        String associatedData = (String) encryptCertificate.get("associatedData");
                        String cipherText = (String) encryptCertificate.get("cipherText");
                        encryptCerts.add(new EncryptCertificate(algorithm, nonce, associatedData, cipherText));
                    }
                }
            }
        }
        return encryptCerts;
    }
}
