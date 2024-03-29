/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.inter.security.cert.parser;

import com.yeepay.yop.sdk.base.security.cert.parser.AbstractYopPublicKeyParser;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParser;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.inter.utils.RSAKeyUtils;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.security.cert.YopPublicKey;
import org.apache.commons.lang3.StringUtils;

/**
 * title: Rsa公钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopRsa2048PublicKeyParser extends AbstractYopPublicKeyParser implements YopCertParser {

    @Override
    public YopPublicKey parse(YopCertConfig certConfig) {
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("ConfigProblem, YopPublicKey StoreType IsNull, certConfig:" + certConfig);
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    return new YopPublicKey(RSAKeyUtils.string2PublicKey(certConfig.getValue()));
                } catch (Exception ex) {
                    throw new YopClientException("ConfigProblem, YopPublicKey Parse Fail, certConfig:" + certConfig + ", ex", ex);
                }
            case FILE_CER:
                try {
                    return new YopPublicKey(getX509Cert(certConfig.getValue(), CertTypeEnum.RSA2048));
                } catch (Exception e) {
                    throw new YopClientException("ConfigProblem, YopPublicKey Parse Fail, certConfig:" + certConfig + ", ex", e);
                }
            default:
                throw new YopClientException("ConfigProblem, YopPublicKey StoreType NotSupport, certConfig:" + certConfig);
        }
    }

    @Override
    public String parserId() {
        return StringUtils.joinWith(CharacterConstants.COMMA, YopCertCategory.PUBLIC, CertTypeEnum.RSA2048);
    }
}
