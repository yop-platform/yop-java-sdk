/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.security.cert.parser;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.base.security.cert.parser.AbstractYopPrivateKeyParser;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParser;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.gm.utils.Sm2Utils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;

/**
 * title: Sm2私钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopSm2PrivateKeyParser extends AbstractYopPrivateKeyParser implements YopCertParser {

    @Override
    public PrivateKey parse(YopCertConfig certConfig) {
        if (null == certConfig.getStoreType()) {
            throw new YopClientException("Can't init ISV private key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    return Sm2Utils.string2PrivateKey(certConfig.getValue());
                } catch (Exception ex) {
                    throw new YopClientException("Failed to init private key form config file is error, " + certConfig, ex);
                }
            case FILE_P12:
                try {
                    return parsePrivateKey(certConfig.getPassword(), certConfig.getValue());
                } catch (Exception ex) {
                    throw new YopClientException("Config wrong for private_key, cert_config:" + certConfig, ex);
                }
            default:
                throw new YopClientException("Config wrong for cert store_type not supported, " + certConfig.getStoreType());
        }
    }

    @Override
    public String parserId() {
        return StringUtils.joinWith(CharacterConstants.COMMA, YopCertCategory.PRIVATE, CertTypeEnum.SM2);
    }

    @Override
    protected KeyStore getKeyStore() throws KeyStoreException, NoSuchProviderException {
        return KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
    }
}
