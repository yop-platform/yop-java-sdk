/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.auth.credentials.provider.YopFixedCredentialsProvider;
import com.yeepay.yop.sdk.config.YopAppConfig;
import com.yeepay.yop.sdk.config.enums.CertStoreType;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.util.LinkedList;
import java.util.List;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2020/11/25 上午10:39
 */
public class CustomFixedCredentialsProvider extends YopFixedCredentialsProvider {

    @Override
    protected YopAppConfig loadAppConfig(String appKey) {
        YopAppConfig yopAppConfig = new YopAppConfig();
        yopAppConfig.setAppKey(appKey);

        YopCertConfig yopCertConfig = new YopCertConfig();
        CertTypeEnum certType = CredentialsRepository.getSupportCertType(appKey);
        yopCertConfig.setCertType(certType);
        yopCertConfig.setStoreType(CertStoreType.STRING);
        yopCertConfig.setValue(CredentialsRepository.getPrivateKeyStr(appKey));

        List<YopCertConfig> isvPrivateKeys = new LinkedList<>();
        isvPrivateKeys.add(yopCertConfig);
        yopAppConfig.setIsvPrivateKey(isvPrivateKeys);

        if (certType == CertTypeEnum.SM2) {
            YopCertConfig isvEncryptKey = new YopCertConfig();
            isvEncryptKey.setCertType(CertTypeEnum.SM4);
            isvEncryptKey.setStoreType(CertStoreType.STRING);
            isvEncryptKey.setValue(CredentialsRepository.getEncryptKeyStr(appKey));
            List<YopCertConfig> isvEncryptKeyList = new LinkedList<>();
            isvEncryptKeyList.add(isvEncryptKey);
            yopAppConfig.setIsvEncryptKeyList(isvEncryptKeyList);
        }
        return yopAppConfig;
    }

    @Override
    public void removeConfig(String key) {

    }
}
