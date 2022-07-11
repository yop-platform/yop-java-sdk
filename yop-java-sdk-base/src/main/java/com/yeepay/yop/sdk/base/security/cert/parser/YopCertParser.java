/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.cert.parser;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;

/**
 * title: 密钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public interface YopCertParser {

    /**
     * 解析密钥
     *
     * @param certConfig   密钥配置
     * @return Object
     */
    Object parse(YopCertConfig certConfig);

    /**
     * 支持的密钥格式
     *
     * @return String
     */
    String parserId();
}
