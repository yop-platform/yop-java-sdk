/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptionsEnhancer;

/**
 * title: 加密选项增强处理抽象<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/27
 */
public abstract class AbstractEncryptOptionsEnhancer implements EncryptOptionsEnhancer {

    protected boolean checkForEnhance(EncryptOptions source) {
        if (null == source.getCredentials()) {
            throw new YopClientException("yop encrypt credentials not specified");
        }
        if (source.getCredentials() instanceof YopSymmetricCredentials) {
            return true;
        }
        LOGGER.warn("credentials not enhanced, class:{}", source.getCredentials().getClass().getCanonicalName());
        return false;
    }
}
