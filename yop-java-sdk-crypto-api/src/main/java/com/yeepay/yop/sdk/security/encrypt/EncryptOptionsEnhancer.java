/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: 加密选项增强处理<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/13
 */
public interface EncryptOptionsEnhancer {

    Logger LOGGER = LoggerFactory.getLogger(EncryptOptionsEnhancer.class);

    /**
     * 增加加密选项
     *
     * @param source 源加密选项
     * @return 增强后的加密选项
     */
    EncryptOptions enhance(EncryptOptions source);
}
