package com.yeepay.yop.sdk.client;

/**
 * title: client处理器参数<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/27 11:11
 */
public class ClientHandlerParams {

    private ClientParams clientParams;

    public ClientParams getClientParams() {
        return clientParams;
    }

    public ClientHandlerParams withClientParams(ClientParams clientParams) {
        this.clientParams = clientParams;
        return this;
    }
}
