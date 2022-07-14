/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback.protocol;

import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorFactory;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.yeepay.yop.sdk.YopConstants.SM4_CALLBACK_ALGORITHM;

/**
 * title: Yop-SM4回调<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/16
 */
public class YopSm4CallbackProtocol extends AbstractYopCallbackProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSm4CallbackProtocol.class);

    /**
     * 算法
     */
    private String algorithm = SM4_CALLBACK_ALGORITHM;

    /**
     * 密钥类型
     */
    private CertTypeEnum certType = CertTypeEnum.SM4;

    /**
     * appKey
     */
    private String customerIdentification;

    /**
     * 加密后的业务参数
     */
    private String cipherText;

    /**
     * 随机iv
     */
    private String nonce;

    /**
     * 附加信息
     */
    private String associatedData;

    @Override
    public YopCallback parse() {
        final String encryptAlg = "SM4/GCM/NoPadding";
        final List<YopCertConfig> isvEncryptKeys = YopCredentialsProviderRegistry.getProvider().getIsvEncryptKey(customerIdentification);
        if (CollectionUtils.isEmpty(isvEncryptKeys)) {
            throw new YopClientException("no isvEncryptKeys found for appKey:" + customerIdentification);
        }

        for (YopCertConfig isvEncryptKey : isvEncryptKeys) {
            try {
                String plainText = YopEncryptorFactory.getEncryptor(encryptAlg).decryptFromBase64(cipherText,
                        new EncryptOptions(new YopSymmetricCredentials(isvEncryptKey.getValue()), "",
                                encryptAlg, nonce, associatedData, BigParamEncryptMode.stream));
                if (null != plainText) {
                    return YopCallback.builder().withId(UUID.randomUUID().toString()).
                            withAppKey(customerIdentification).withType(originRequest.getHttpPath())
                            .withCreateTime(new Date()).withBizData(plainText)
                            .withMetaInfo("headers", originRequest.getHeaders()).build();
                }
            } catch (Exception e) {
                LOGGER.warn("fail to parse YopSm4CallbackProtocol with key:" +  isvEncryptKey.getValue() + ", ex:", e);
            }
        }
        throw new YopClientException("fail to parse YopSm4CallbackProtocol, appKey:" + customerIdentification);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getCustomerIdentification() {
        return customerIdentification;
    }

    public String getCipherText() {
        return cipherText;
    }

    public String getNonce() {
        return nonce;
    }

    public String getAssociatedData() {
        return associatedData;
    }

    public YopSm4CallbackProtocol setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public YopSm4CallbackProtocol setCertType(CertTypeEnum certType) {
        this.certType = certType;
        return this;
    }

    public YopSm4CallbackProtocol setCustomerIdentification(String customerIdentification) {
        this.customerIdentification = customerIdentification;
        return this;
    }

    public YopSm4CallbackProtocol setCipherText(String cipherText) {
        this.cipherText = cipherText;
        return this;
    }

    public YopSm4CallbackProtocol setNonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    public YopSm4CallbackProtocol setAssociatedData(String associatedData) {
        this.associatedData = associatedData;
        return this;
    }
}
