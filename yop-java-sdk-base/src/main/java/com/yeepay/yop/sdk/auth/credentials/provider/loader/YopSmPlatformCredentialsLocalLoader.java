/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider.loader;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentialsHolder;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.EnvUtils;
import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.Sm2CertUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_PLATFORM_CERT_POSTFIX;
import static com.yeepay.yop.sdk.YopConstants.YOP_SM_PLATFORM_CERT_PREFIX;

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
public class YopSmPlatformCredentialsLocalLoader implements YopPlatformCredentialsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSmPlatformCredentialsLocalLoader.class);

    private YopPlatformCredentialsLoader delegate = new YopSmPlatformCredentialsRemoteLoader();

    protected X509Certificate cfcaRoot, yopInter;
    private String defaultCertPath = "config/certs", defaultCfcaRootFile = "cfca_root.pem",
            defaultYopInterFile = "yop_inter.pem";
    private YopCertStore defaultYopCertStore;

    {
        try {
            if (!EnvUtils.isProd()) {
                String env = EnvUtils.currentEnv(),
                        envPrefix = StringUtils.substringBefore(env, "_");
                defaultCfcaRootFile = envPrefix + "_" + defaultCfcaRootFile;
                defaultYopInterFile = envPrefix + "_" + defaultYopInterFile;
            }
            defaultYopCertStore = new YopCertStore(defaultCertPath);

            cfcaRoot = getX508Cert(defaultCertPath + "/" + defaultCfcaRootFile);
            verifyCertChain("cfca root", null, cfcaRoot);

            yopInter = getX508Cert(defaultCertPath + "/" + defaultYopInterFile);
            verifyCertChain("yop inter", (BCECPublicKey) cfcaRoot.getPublicKey(), yopInter);
        } catch (Exception e) {
            LOGGER.error("error when load parent certs, ex:", e);
        }
    }

    private void verifyCertChain(String certName, BCECPublicKey issuer, X509Certificate cert) {
        try {
            Sm2CertUtils.verifyCertificate(issuer, cert);
        } catch (Exception e) {
            throw new YopClientException("invalid " + certName + " cert, detail:" + e.getMessage());
        }
    }

    private X509Certificate getX508Cert(String certPath) throws CertificateException, NoSuchProviderException {
        InputStream certStream = null;
        try {
            certStream = FileUtils.getResourceAsStream(certPath);
            return Sm2CertUtils.getX509Certificate(certStream);
        } finally {
            StreamUtils.closeQuietly(certStream);
        }
    }

    @Override
    public Map<String, YopPlatformCredentials> load(String appKey, String serialNo) {
        // 从本地指定目录加载
        YopCertStore yopCertStore = YopSdkConfigProviderRegistry.getProvider().getConfig().getYopCertStore();
        Map<String, X509Certificate> localCerts = loadAndVerifyFromLocal(yopCertStore, serialNo);
        Map<String, YopPlatformCredentials> localCredentials = new LinkedHashMap<>();

        // 尝试从当前目录加载
        if (MapUtils.isEmpty(localCerts)) {
            localCerts = loadAndVerifyFromLocal(defaultYopCertStore, serialNo);
        }
        if (MapUtils.isNotEmpty(localCerts)) {
            localCerts.forEach((k, v) -> localCredentials.put(k, new YopPlatformCredentialsHolder()
                    .withSerialNo(serialNo).withPublicKey(CertTypeEnum.SM2, v.getPublicKey())));
            if (localCredentials.containsKey(serialNo)) {
                return localCredentials;
            }
        }
        LOGGER.info("no available sm2 cert from local, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);

        // 从远程加载
        return delegate.load(appKey, serialNo);
    }

    private Map<String, X509Certificate> loadAndVerifyFromLocal(YopCertStore yopCertStore, String serialNo) {
        LOGGER.debug("load sm2 cert from local, path:{}, serialNo:{}", yopCertStore.getPath(), serialNo);
        Map<String, X509Certificate> certMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(yopCertStore.getPath()) && BooleanUtils.isTrue(yopCertStore.getEnable())) {
            InputStream fis = null;
            try {
                String filename = yopCertStore.getPath() + "/" + YOP_SM_PLATFORM_CERT_PREFIX + serialNo + YOP_PLATFORM_CERT_POSTFIX;
                fis = FileUtils.getResourceAsStream(filename);
                if (null != fis) {
                    final X509Certificate cert = Sm2CertUtils.getX509Certificate(fis);

                    Sm2CertUtils.verifyCertificate((BCECPublicKey) yopInter.getPublicKey(), cert);
                    String realSerialNo = cert.getSerialNumber().toString();
                    if (!realSerialNo.equals(serialNo)) {
                        LOGGER.warn("wrong file name for cert, serialNo:{}, realSerialNo:{}", serialNo, realSerialNo);
                    }
                    certMap.put(realSerialNo, cert);
                }
            } catch (Exception e) {
                LOGGER.error("error when load cert from local file, serialNo:" + serialNo + ", ex:", e);
            } finally {
                StreamUtils.closeQuietly(fis);
            }
        }
        return certMap;
    }

}
