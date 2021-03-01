/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.security.DigestAlgEnum;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 4:00 下午
 */
public interface YopSignProcess {
    /**
     * 签名
     *
     * @param content
     * @param credentialsItem
     * @return
     */
    String sign(String content, PKICredentialsItem credentialsItem);

    /**
     * 验签
     *
     * @param content
     * @param signature
     * @param credentialsItem
     * @return
     */
    boolean verify(String content, String signature, PKICredentialsItem credentialsItem);

    /**
     * 获取摘要算法
     *
     * @return
     */
    DigestAlgEnum getDigestAlg();
}
