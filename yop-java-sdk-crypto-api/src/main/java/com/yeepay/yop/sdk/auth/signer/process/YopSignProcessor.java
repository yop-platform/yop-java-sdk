/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.exception.YopClientException;

/**
 * title: YopSignProcessor<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 4:00 下午
 */
public interface YopSignProcessor {

    /**
     * 签名
     *
     * @param content         签名原文
     * @param credentialsItem 签名密钥信息
     * @return urlSafeBase64编码的字符串
     */
    default String sign(String content, CredentialsItem credentialsItem) {
        if (!isSupport(credentialsItem)) {
            throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
        }
        return doSign(content, credentialsItem);
    }

    default String doSign(String content, CredentialsItem credentialsItem) {
        return doSign(content, credentialsItem, null);
    }

    /**
     * 签名
     *
     * @param content         签名原文
     * @param credentialsItem 签名密钥信息
     * @param options         签名选项
     * @return base64编码的字符串(是否urlSafe ， 可在options中指定)
     */
    String doSign(String content, CredentialsItem credentialsItem, SignOptions options);

    /**
     * 验签
     *
     * @param content 签名原文
     * @param signature 签名
     * @param credentialsItem 签名密钥信息
     * @return true: 验签通过，false: 不通过
     */
    default boolean verify(String content, String signature, CredentialsItem credentialsItem) {
        if (isSupport(credentialsItem)) {
            return doVerify(content, signature, credentialsItem);
        }
        throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
    }

    default boolean doVerify(String content, String signature, CredentialsItem credentialsItem) {
        return doVerify(content, signature, credentialsItem, null);
    }

    /**
     * 验签
     *
     * @param content         签名原文
     * @param signature       签名
     * @param credentialsItem 签名密钥信息
     * @param options         签名选项
     * @return true: 验签通过，false: 不通过
     */
    boolean doVerify(String content, String signature, CredentialsItem credentialsItem, SignOptions options);

    /**
     * 判断是否支持用该密钥进行签名/验签
     *
     * @param credentialsItem 密钥信息
     * @return true: 支持，false：不支持
     */
    boolean isSupport(CredentialsItem credentialsItem);

    /**
     * 签名处理器标识
     *
     * @return 签名器标识
     */
    String name();

    /**
     * 获取摘要算法
     *
     * @return 摘要算法
     */
    String getDigestAlg();
}
