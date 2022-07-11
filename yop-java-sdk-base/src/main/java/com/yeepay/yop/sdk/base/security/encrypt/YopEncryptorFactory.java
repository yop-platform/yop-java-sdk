/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: 加解密器工厂类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/11
 */
public class YopEncryptorFactory {

    /**
     * 加解密器Map
     * key: 加解密算法
     * value: 加解密器
     */
    private static final Map<String, YopEncryptor> YOP_ENCRYPTOR_MAP = Maps.newHashMap();

    static {
        ServiceLoader<YopEncryptor> serviceLoader = ServiceLoader.load(YopEncryptor.class);
        for (YopEncryptor encryptor : serviceLoader) {
            for (String encryptAlg : encryptor.supportedAlgs()) {
                YOP_ENCRYPTOR_MAP.put(encryptAlg, encryptor);
            }
        }
    }

    /**
     * 扩展算法
     *
     * @param encryptAlg 算法名称
     * @param encryptor  加解密器
     */
    public static void registerEncryptor(String encryptAlg, YopEncryptor encryptor) {
        YOP_ENCRYPTOR_MAP.put(encryptAlg, encryptor);
    }

    /**
     * 扩展算法
     *
     * @param encryptAlgs 算法名称列表
     * @param encryptor   加解密器
     */
    public static void registerEncryptor(List<String> encryptAlgs, YopEncryptor encryptor) {
        for (String encryptAlg : encryptAlgs) {
            registerEncryptor(encryptAlg, encryptor);
        }
    }

    /**
     * 扩展算法
     *
     * @param encryptors <加解密算法,加解密器>
     */
    public static void registerEncryptor(Map<String, YopEncryptor> encryptors) {
        YOP_ENCRYPTOR_MAP.putAll(encryptors);
    }

    /**
     * 根据加解密算法获取加密器
     *
     * @param encryptAlg 加解密算法
     * @return 加解密器
     */
    public static YopEncryptor getEncryptor(String encryptAlg) {
        final YopEncryptor yopEncryptor = YOP_ENCRYPTOR_MAP.get(encryptAlg);
        if (null == yopEncryptor) {
            throw new YopClientException("YopEncryptor not found, encryptAlg:" + encryptAlg);
        }
        return yopEncryptor;
    }

}
