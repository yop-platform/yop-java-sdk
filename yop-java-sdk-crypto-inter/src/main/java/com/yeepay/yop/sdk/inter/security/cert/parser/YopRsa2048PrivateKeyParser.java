/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.inter.security.cert.parser;

import com.yeepay.yop.sdk.base.security.cert.parser.AbstractYopPrivateKeyParser;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParser;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.config.IllegalConfigFormatException;
import com.yeepay.yop.sdk.inter.utils.RSAKeyUtils;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import org.apache.commons.lang3.StringUtils;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;

/**
 * title: Rsa私钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopRsa2048PrivateKeyParser extends AbstractYopPrivateKeyParser implements YopCertParser {

    @Override
    public PrivateKey parse(YopCertConfig certConfig) {
        if (null == certConfig.getStoreType()) {
            throw new IllegalConfigFormatException("store_type", "ConfigProblem, IsvPrivateKey StoreType IsNull, certConfig:" + certConfig);
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    return RSAKeyUtils.string2PrivateKey(certConfig.getValue());
                } catch (Exception ex) {
                    throw new IllegalConfigFormatException("value",
                            "ConfigProblem, IsvPrivateKey Parse Fail, certConfig:"
                                    + certConfig + ", cause:" + ex.getMessage(), ex);
                }
            case FILE_P12:
                try {
                    return parsePrivateKey(certConfig.getPassword(), certConfig.getValue());
                } catch (Exception ex) {
                    throw new IllegalConfigFormatException("value", "ConfigProblem, IsvPrivateKey Parse Fail, certConfig:"
                            + certConfig + ", cause:" + ex.getMessage(), ex);
                }
            default:
                throw new IllegalConfigFormatException("store_type", "ConfigProblem, IsvPrivateKey StoreType NotSupport, certConfig:" + certConfig);
        }
    }

    @Override
    public String parserId() {
        return StringUtils.joinWith(CharacterConstants.COMMA, YopCertCategory.PRIVATE, CertTypeEnum.RSA2048);
    }

    @Override
    protected KeyStore getKeyStore() throws KeyStoreException {
        return KeyStore.getInstance("PKCS12");
    }
}
