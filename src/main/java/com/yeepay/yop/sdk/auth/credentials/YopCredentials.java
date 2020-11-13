package com.yeepay.yop.sdk.auth.credentials;


import java.io.Serializable;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/2 10:38
 */
public interface YopCredentials extends Serializable {

    /**
     * Returns the YOP app key ID for this credentials object.
     *
     * @return the YOP app key for this credentials object.
     */
    String getAppKey();

    /**
     * Returns the Yop secret access key for this credentials object.
     *
     * @return The Yop secret access key for this credentials object.
     */
    String getSecretKey();

}
