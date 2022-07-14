/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.cert;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: x509证书支持类工厂<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class X509CertSupportFactory {

    /**
     * x509证书支持类Map
     * <p>
     * key: 密钥类型
     * value: x509证书支持类
     */
    private static final Map<String, X509CertSupport> X509_CERT_GENERATOR_MAP = Maps.newHashMap();

    static {
        ServiceLoader<X509CertSupport> serviceLoader = ServiceLoader.load(X509CertSupport.class);
        for (X509CertSupport x509CertSupport : serviceLoader) {
            X509_CERT_GENERATOR_MAP.put(x509CertSupport.support(), x509CertSupport);
        }
    }

    /**
     * 根据密钥类型获取x509证书支持类
     *
     * @param certType 密钥类型
     * @return x509证书支持类
     */
    public static X509CertSupport getSupport(String certType) {
        final X509CertSupport x509CertSupport = X509_CERT_GENERATOR_MAP.get(certType);
        if (null == x509CertSupport) {
            throw new YopClientException("X509CertSupport not found, certType:" + certType);
        }
        return x509CertSupport;
    }

    /**
     * 扩展x509证书支持类
     *
     * @param certType      密钥类型
     * @param certGenerator x509证书支持类
     */
    public static void registerSupport(String certType, X509CertSupport certGenerator) {
        X509_CERT_GENERATOR_MAP.put(certType, certGenerator);
    }

}
