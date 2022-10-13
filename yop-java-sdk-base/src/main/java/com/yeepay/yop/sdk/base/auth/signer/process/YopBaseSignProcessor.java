/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.signer.process;

import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.exception.YopClientException;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2021/12/23 8:37 下午
 */
public abstract class YopBaseSignProcessor implements YopSignProcessor {

    /**
     * 签名
     *
     * @param content         签名原文
     * @param credentialsItem 签名密钥信息
     * @return urlSafeBase64编码的字符串
     */
    @Override
    public String sign(String content, CredentialsItem credentialsItem) {
        if (!isSupport(credentialsItem)) {
            throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
        }
        return doSign(content, credentialsItem);
    }

    @Override
    public String doSign(String content, CredentialsItem credentialsItem) {
        return doSign(content, credentialsItem, null);
    }

    /**
     * 验签
     *
     * @param content 签名原文
     * @param signature 签名
     * @param credentialsItem 签名密钥信息
     * @return true: 验签通过，false: 不通过
     */
    @Override
    public boolean verify(String content, String signature, CredentialsItem credentialsItem) {
        if (isSupport(credentialsItem)) {
            return doVerify(content, signature, credentialsItem);
        }
        throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
    }

    @Override
    public boolean doVerify(String content, String signature, CredentialsItem credentialsItem) {
        return doVerify(content, signature, credentialsItem, null);
    }
}
