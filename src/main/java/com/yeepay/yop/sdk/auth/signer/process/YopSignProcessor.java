/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

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
     * @param content
     * @param credentialsItem
     * @return
     */
    default String sign(String content, CredentialsItem credentialsItem) {
        if (!isSupport(credentialsItem)) {
            throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
        }
        return doSign(content, credentialsItem);
    }

    String doSign(String content, CredentialsItem credentialsItem);

    /**
     * 验签
     *
     * @param content
     * @param signature
     * @param credentialsItem
     * @return
     */
    default boolean verify(String content, String signature, CredentialsItem credentialsItem) {
        if (isSupport(credentialsItem)) {
            return doVerify(content, signature, credentialsItem);
        }
        throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
    }

    boolean doVerify(String content, String signature, CredentialsItem credentialsItem);

    boolean isSupport(CredentialsItem credentialsItem);

    /**
     * 签名处理器名称
     *
     * @return
     */
    String name();

    /**
     * 获取摘要算法
     *
     * @return
     */
    String getDigestAlg();
}
