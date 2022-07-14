/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.cert.parser;

import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;

/**
 * title: Yop公钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public abstract class AbstractYopPrivateKeyParser {

    /**
     * 解析p12文件
     *
     * @param keyStorePwd  密码
     * @param keyStorePath 文件路径
     * @return PrivateKey
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     */
    protected PrivateKey parsePrivateKey(String keyStorePwd, String keyStorePath)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException {
        String pwd = StringUtils.defaultIfEmpty(keyStorePwd, "");
        char[] password = pwd.toCharArray();
        KeyStore keystore = getKeyStore();
        InputStream keyStream = null;
        try {
            keyStream = FileUtils.getResourceAsStream(keyStorePath);
            keystore.load(keyStream, password);
        } finally {
            StreamUtils.closeQuietly(keyStream);
        }
        Enumeration<?> aliases = keystore.aliases();
        String keyAlias;
        Key key = null;
        while (aliases.hasMoreElements() && !(key instanceof PrivateKey)) {
            keyAlias = (String) aliases.nextElement();
            key = keystore.getKey(keyAlias, password);
        }
        return (PrivateKey) key;
    }

    protected abstract KeyStore getKeyStore() throws KeyStoreException, NoSuchProviderException;
}
