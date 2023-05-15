package com.yeepay.yop.sdk.auth.credentials;

/**
 * title: AES凭证<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/4 16:51
 */
public class YopAESCredentials extends BaseYopCredentials {

    private static final long serialVersionUID = 5706406781522872546L;

    public YopAESCredentials(String appKey, String secretKey) {
        super(appKey, secretKey);
    }
}
