/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.io.Serializable;
import java.security.PublicKey;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/4/21 5:26 PM
 */
public interface YopPlatformCredentials extends Serializable {

    /**
     * 证书序列号
     *
     * @return
     */
    String getSerialNo();

    /**
     * 证书公钥
     *
     * @param certType 公钥类型
     * @return
     */
    PublicKey getPublicKey(CertTypeEnum certType);
}
