package com.yeepay.yop.sdk.config.support;

import com.yeepay.g3.core.yop.sdk.sample.config.CertConfig;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopServiceException;
import com.yeepay.g3.core.yop.sdk.sample.security.rsa.RSAKeyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * title: 配置工具<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/9/18 17:08
 */
public final class ConfigUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    public static PublicKey loadPublicKey(CertConfig certConfig) {
        PublicKey publicKey;
        if (null == certConfig.getStoreType()) {
            throw new YopServiceException("Can't init YOP public key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    publicKey = RSAKeyUtils.string2PublicKey(certConfig.getValue());
                } catch (Exception ex) {
                    throw new YopServiceException("Failed to load public key form config file is error," + certConfig, ex);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return publicKey;
    }

    public static PrivateKey loadPrivateKey(CertConfig certConfig) {
        PrivateKey privateKey;
        if (null == certConfig.getStoreType()) {
            throw new YopServiceException("Can't init ISV private key! Store type is error.");
        }
        switch (certConfig.getStoreType()) {
            case STRING:
                try {
                    privateKey = RSAKeyUtils.string2PrivateKey(certConfig.getValue());
                } catch (Exception ex) {
                    throw new YopServiceException("Failed to load private key form config file is error, " + certConfig, ex);
                }
                break;
            case FILE_P12:
                try {
                    char[] password = certConfig.getPassword().toCharArray();
                    KeyStore keystore = KeyStore.getInstance("PKCS12");
                    keystore.load(ConfigUtils.class.getResourceAsStream(certConfig.getValue()), password);

                    Enumeration aliases = keystore.aliases();
                    String keyAlias = "";
                    while (aliases.hasMoreElements()) {
                        keyAlias = (String) aliases.nextElement();
                    }
                    privateKey = (PrivateKey) keystore.getKey(keyAlias, password);
                } catch (Exception ex) {
                    throw new YopServiceException("Cert key is error, " + certConfig, ex);
                }
                break;
            default:
                throw new RuntimeException("Not support cert store type.");
        }
        return privateKey;
    }

    /**
     * 获取dir下面的所有文件
     *
     * @param dir 路径
     * @return 文件全路径集合
     * @throws IOException        io异常
     * @throws URISyntaxException uri异常
     */
    public static List<String> listFiles(String dir) throws IOException, URISyntaxException {
        URL url = StringUtils.startsWith(dir, "file://") ? new URL(dir) :
                getResource(dir);
        if (url == null) {
            return Collections.emptyList();
        } else if (StringUtils.equals(url.getProtocol(), "file")) {
            File file = new File(url.toURI());
            if (file.isDirectory()) {
                List<String> files = new ArrayList<String>();
                InputStream in = url.openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String resource;
                while ((resource = br.readLine()) != null) {
                    files.add(dir + File.separator + resource);
                }
                return files;
            } else {
                throw new IllegalArgumentException(dir + " is not a dir.");
            }
        } else {
            LOGGER.info("can't read dir from protocol:" + url.getProtocol());
            return Collections.emptyList();
        }
    }

    public static InputStream getInputStream(String location) throws FileNotFoundException {
        InputStream fis;
        if (StringUtils.startsWith(location, "file://")) {
            fis = new FileInputStream(StringUtils.substring(location, 6));
        } else {
            fis = getResourceAsStream(location);
        }
        return fis;
    }

    public static InputStream getResourceAsStream(String resource) {
        if (StringUtils.startsWith(resource, "/")) {
            resource = StringUtils.substring(resource, 1);
        }
        final InputStream in = getContextClassLoader().getResourceAsStream(resource);
        return in == null ? ConfigUtils.class.getResourceAsStream("/" + resource) : in;
    }

    public static URL getResource(String resource) {
        if (StringUtils.startsWith(resource, "/")) {
            resource = StringUtils.substring(resource, 1);
        }
        final URL url = getContextClassLoader().getResource(resource);
        return url == null ? ConfigUtils.class.getResource("/" + resource) : url;
    }

    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}