/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.digest;

import java.io.InputStream;
import java.util.List;

/**
 * title: 摘要计算<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/6/26
 */
public interface YopDigester {

    /**
     * 支持的算法列表
     *
     * @return 算法列表
     */
    List<String> supportedAlgs();


    /**
     * 计算摘要
     *
     * @param input 输入流
     * @param alg 摘要算法
     * @return byte[]
     */
    byte[] digest(InputStream input, String alg);

}
