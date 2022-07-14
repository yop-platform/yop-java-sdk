/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.base.security.cert.parser;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.base.security.cert.parser.AbstractYopPublicKeyParser;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParser;
import com.yeepay.yop.sdk.security.cert.YopPublicKey;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * title: Sm2密钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopSm2PublicKeyParser extends AbstractYopPublicKeyParser implements YopCertParser {

    @Override
    public YopPublicKey parse(YopCertConfig certConfig) {
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("Can't init YOP public key!,  Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    return new YopPublicKey(SmUtils.string2PublicKey(certConfig.getValue()));
                } catch (Exception ex) {
                    throw new YopClientException("Can't init YOP public key!, cert_config:" + certConfig, ex);
                }
            case FILE_CER:
                try {
                    return new YopPublicKey(getX509Cert(certConfig.getValue(), CertTypeEnum.SM2));
                } catch (Exception e) {
                    throw new YopClientException("Can't init YOP public key! cert_config:" + certConfig, e);
                }
            default:
                throw new YopClientException("Can't init YOP public key!, cert_config:" + certConfig);
        }
    }

    @Override
    public String parserId() {
        return StringUtils.joinWith(CharacterConstants.COMMA, YopCertCategory.PUBLIC, CertTypeEnum.SM2);
    }

}
