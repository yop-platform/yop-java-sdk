package com.yeepay.yop.sdk.config.provider.file.support;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.Sm2CertUtils;
import com.yeepay.yop.sdk.utils.Sm2Utils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
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
                if (CertTypeEnum.RSA2048 == yopCertConfig.getCertType()) {
                    publicKey = RSAKeyUtils.string2PublicKey(yopCertConfig.getValue());
                } else {
                    publicKey = Sm2Utils.string2PublicKey(yopCertConfig.getValue());
                }
                break;
            case FILE_CER:
                InputStream inputStream = FileUtils.getResourceAsStream(yopCertConfig.getValue());
                try {
                    X509Certificate x509Certificate = Sm2CertUtils.getX509Certificate(inputStream);
                    publicKey = x509Certificate.getPublicKey();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return publicKey;
    }

    public static String loadPrivateKey(YopCertConfig yopCertConfig) {
        String privateKey = null;
        if (null == yopCertConfig.getStoreType()) {
            throw new YopServiceException("Can't init ISV private key! Store type is error.");
        }
        switch (yopCertConfig.getStoreType()) {
            case STRING:
                try {
                    privateKey = yopCertConfig.getValue();
                } catch (Exception ex) {
                    throw new YopServiceException("Failed to load private key form config file is error, " + yopCertConfig, ex);
                }
                break;
            case FILE_P12:
                try {
                    String pwd = StringUtils.defaultIfEmpty(yopCertConfig.getPassword(), "");
                    char[] password = pwd.toCharArray();
                    KeyStore keystore = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
                    keystore.load(FileUtils.getResourceAsStream(yopCertConfig.getValue()), password);

                    Enumeration aliases = keystore.aliases();
                    String keyAlias;
                    Key key = null;
                    while (aliases.hasMoreElements() && !(key instanceof PrivateKey)) {
                        keyAlias = (String) aliases.nextElement();
                        key = keystore.getKey(keyAlias, password);
                    }
                    if (null != key) {
                        privateKey = RSAKeyUtils.key2String(key);
                    }
                } catch (Exception ex) {
                    throw new YopClientException("Config wrong for private_key, cert_config:" + yopCertConfig, ex);
                }
                break;
            default:
                throw new YopClientException("Config wrong for cert store_type not supported, " + yopCertConfig.getStoreType());
        }
        if (StringUtils.isEmpty(privateKey)) {
            throw new YopClientException("Config wrong for private_key, cert_config:" + yopCertConfig);
        }
        return privateKey;
    }

}

