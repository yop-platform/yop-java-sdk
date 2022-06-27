/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: Yop客户端凭证<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:55 下午
 */
public interface YopCredentials<T> {

    /**
     * 应用标识
     *
     * @return
     */
    String getAppKey();

    /**
     * 获取认证凭证
     *
     * @return
     */
    T getCredential();

}
