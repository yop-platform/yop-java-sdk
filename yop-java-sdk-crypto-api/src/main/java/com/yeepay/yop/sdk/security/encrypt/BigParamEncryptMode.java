/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt;

/**
 * title: 大参数(文件)加密模式<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public enum BigParamEncryptMode {
    /**
     * 文件 分块加密
     */
    chunked,

    /**
     * 文件 流加密
     */
    stream
}
