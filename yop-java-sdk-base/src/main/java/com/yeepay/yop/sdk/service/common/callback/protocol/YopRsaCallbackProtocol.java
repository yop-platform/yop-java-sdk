/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback.protocol;

import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.utils.DigitalEnvelopeUtils;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;

import java.util.Date;
import java.util.UUID;

/**
 * title: Yop-RSA回调<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/16
 */
public class YopRsaCallbackProtocol extends AbstractYopCallbackProtocol {

    /**
     * appKey
     */
    private String customerIdentification;


    /**
     * 加密的业务数据
     */
    private String response;

    @Override
    public YopCallback parse() {
        return YopCallback.builder().withId(UUID.randomUUID().toString()).
                withAppKey(customerIdentification).withType(originRequest.getHttpPath())
                .withCreateTime(new Date()).withBizData(DigitalEnvelopeUtils.decrypt(response, customerIdentification, CertTypeEnum.RSA2048.getValue()))
                .withMetaInfo("headers", originRequest.getHeaders()).build();
    }

    public String getCustomerIdentification() {
        return customerIdentification;
    }

    public String getResponse() {
        return response;
    }

    public YopRsaCallbackProtocol setCustomerIdentification(String customerIdentification) {
        this.customerIdentification = customerIdentification;
        return this;
    }

    public YopRsaCallbackProtocol setResponse(String response) {
        this.response = response;
        return this;
    }
}
