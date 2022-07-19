/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: 证书凭证<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/6/27
 */
public interface CertificateCredentials {

    /**
     * 证书序列号(长度为10的16进制字符串)
     *
     * 比如，目前易宝证书序列号如下表示
     *   16进制表示为：4397139598
     *   10进制表示：290297451928
     *
     * @return 请将证书序列号转换为16进制表示，并返回
     */
    String getSerialNo();
}
