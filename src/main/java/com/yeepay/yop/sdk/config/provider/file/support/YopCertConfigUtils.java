package com.yeepay.yop.sdk.config.provider.file.support;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.FileUtils;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

/**
 * title: sdk配置支持类<br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class YopCertConfigUtils {

    public static PublicKey loadPublicKey(YopCertConfig yopCertConfig) {
        PublicKey publicKey;
        if (null == yopCertConfig.getStoreType()) {
            throw new YopServiceException("Can't init YOP public key! Store type is error.");
        }
        switch (yopCertConfig.getStoreType()) {
            case STRING:
                publicKey = RSAKeyUtils.string2PublicKey(yopCertConfig.getValue());
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return publicKey;
    }

    public static PrivateKey loadPrivateKey(YopCertConfig yopCertConfig) {
        PrivateKey privateKey;
        if (null == yopCertConfig.getStoreType()) {
            throw new YopServiceException("Can't init ISV private key! Store type is error.");
        }
        switch (yopCertConfig.getStoreType()) {
            case STRING:
                try {
                    privateKey = RSAKeyUtils.string2PrivateKey(yopCertConfig.getValue());
                } catch (Exception ex) {
                    throw new YopServiceException("Failed to load private key form config file is error, " + yopCertConfig, ex);
                }
                break;
            case FILE_P12:
                try {
                    char[] password = yopCertConfig.getPassword().toCharArray();
                    KeyStore keystore = KeyStore.getInstance("PKCS12");
                    keystore.load(FileUtils.getResourceAsStream(yopCertConfig.getValue()), password);

                    Enumeration aliases = keystore.aliases();
                    String keyAlias = "";
                    while (aliases.hasMoreElements()) {
                        keyAlias = (String) aliases.nextElement();
                    }
                    privateKey = (PrivateKey) keystore.getKey(keyAlias, password);
                } catch (Exception ex) {
                    throw new YopServiceException("Cert key is error, " + yopCertConfig, ex);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return privateKey;
    }

}

