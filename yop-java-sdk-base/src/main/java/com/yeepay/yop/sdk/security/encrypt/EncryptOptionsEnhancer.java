/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt;

import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSm4Credentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.yeepay.yop.sdk.YopConstants.*;

/**
 * title: 加密选项增强处理<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/13
 */
public interface EncryptOptionsEnhancer {

    Logger LOGGER = LoggerFactory.getLogger(EncryptOptionsEnhancer.class);

    /**
     * 增加加密选项
     *
     * @param source 源加密选项
     * @return 增强后的加密选项
     */
    EncryptOptions enhance(EncryptOptions source);

    abstract class AbstractEncryptOptionsEnhancer implements EncryptOptionsEnhancer {

        protected boolean checkForEnhance(EncryptOptions source) {
            if (null == source.getCredentials()) {
                throw new YopClientException("yop encrypt credentials not specified");
            }
            if (source.getCredentials() instanceof YopSm4Credentials) {
                return true;
            }
            LOGGER.warn("credentials not enhanced, class:{}", source.getCredentials().getClass().getCanonicalName());
            return false;
        }
    }

    class Sm2Enhancer extends AbstractEncryptOptionsEnhancer {
        private final String appKey;

        public Sm2Enhancer(String appKey) {
            this.appKey = appKey;
        }

        @Override
        public EncryptOptions enhance(EncryptOptions source) {
            if (!checkForEnhance(source)) {
                return source;
            }
            YopSm4Credentials sourceCredentials = (YopSm4Credentials) source.getCredentials();
            String credentialStr = sourceCredentials.getCredential();
            final YopPlatformCredentials yopPlatformCredentials = YopPlatformCredentialsProviderRegistry.getProvider()
                    .getLatestAvailable(appKey, CertTypeEnum.SM2.getValue());
            source.setEncryptedCredentials(YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_SM2).encryptToBase64(credentialStr,
                    new EncryptOptions(yopPlatformCredentials)));
            source.setCredentialsAlg(YOP_CREDENTIALS_ENCRYPT_ALG_SM2);
            source.setCredentials(new YopSm4Credentials(appKey, credentialStr));
            source.enhance(YOP_ENCRYPT_OPTIONS_YOP_SM2_CERT_SERIAL_NO, yopPlatformCredentials.getSerialNo());
            return source;
        }
    }

    class Sm4Enhancer extends AbstractEncryptOptionsEnhancer {
        private final String appKey;

        public Sm4Enhancer(String appKey) {
            this.appKey = appKey;
        }

        @Override
        public EncryptOptions enhance(EncryptOptions source) {
            if (!checkForEnhance(source)) {
                return source;
            }
            List<YopCertConfig> mainKeys = YopCredentialsProviderRegistry.getProvider().getIsvEncryptKey(appKey);
            if (CollectionUtils.isEmpty(mainKeys)) {
                throw new YopClientException("isv encrypted key not config");
            }
            EncryptOptions mainKeyOptions = source.copy();
            String mainCredential = mainKeys.get(0).getValue();
            mainKeyOptions.setCredentials(new YopSm4Credentials(appKey, mainCredential));

            String credentialStr = ((YopSm4Credentials) source.getCredentials()).getCredential();
            source.setEncryptedCredentials(YopEncryptorFactory.getEncryptor(YOP_CREDENTIALS_ENCRYPT_ALG_SM4)
                    .encryptToBase64(credentialStr, mainKeyOptions));
            source.setCredentials(new YopSm4Credentials(appKey, credentialStr));
            source.setCredentialsAlg(YOP_CREDENTIALS_ENCRYPT_ALG_SM4);
            source.enhance(YOP_ENCRYPT_OPTIONS_YOP_SM4_MAIN_CREDENTIALS, mainCredential);
            return source;
        }
    }
}
