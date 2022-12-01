/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CredentialsCollection;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.exception.YopClientException;

/**
 * title: 签名适配器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/12/2
 */
public abstract class YopSignProcessorAdaptor implements YopSignProcessor {

    @Override
    public String sign(String content, CredentialsItem credentialsItem) {
        return signWithOptions(content, credentialsItem, null);
    }

    @Override
    public boolean verify(String content, String signature, CredentialsItem credentialsItem) {
        return verifyWithOptions(content, signature, credentialsItem, null);
    }

    @Override
    public String signWithOptions(String content, CredentialsItem credentialsItem, SignOptions options) {
        if (!isSupport(credentialsItem)) {
            throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
        }
        return doSign(content, credentialsItem, options);
    }

    @Override
    public boolean verifyWithOptions(String content, String signature, CredentialsItem credentialsItem, SignOptions options) {
        if (credentialsItem instanceof CredentialsCollection) {
            for (CredentialsItem item : ((CredentialsCollection) credentialsItem).getItems()) {
                try {
                    if (isSupport(item) && doVerify(content, signature, item, options)) {
                        return true;
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }
        } else if (isSupport(credentialsItem)){
            return doVerify(content, signature, credentialsItem, options);
        } else {
            throw new YopClientException("UnSupported credentialsItem type:" + credentialsItem.getClass().getSimpleName());
        }
        return false;
    }

    public abstract String doSign(String content, CredentialsItem credentialsItem, SignOptions options);

    public abstract boolean doVerify(String content, String signature, CredentialsItem credentialsItem, SignOptions options);

}
