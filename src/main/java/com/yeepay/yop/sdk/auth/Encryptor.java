package com.yeepay.yop.sdk.auth;

import com.yeepay.yop.sdk.internal.Request;

/**
 * title:加解密器 <br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-18 19:00
 */
public interface Encryptor {

    /**
     * 加密
     *
     * @param request 请求
     */
    void encrypt(Request request);

    /**
     * 解密
     *
     * @param content 密文
     * @return 明文
     */
    String decrypt(String content);
}
