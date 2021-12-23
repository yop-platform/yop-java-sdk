/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
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
public abstract class BaseYopSignProcessor implements YopSignProcessor {

    @Override
    public String sign(String content, CredentialsItem credentialsItem) {
        if (!isSupport(credentialsItem)) {
            throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
        }
        return doSign(content, credentialsItem);
    }

    protected abstract String doSign(String content, CredentialsItem credentialsItem);

    @Override
    public boolean verify(String content, String signature, CredentialsItem credentialsItem) {
        if (isSupport(credentialsItem)) {
            return doVerify(content, signature, credentialsItem);
        }
        throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
    }

    protected abstract boolean doVerify(String content, String signature, CredentialsItem credentialsItem);
}
