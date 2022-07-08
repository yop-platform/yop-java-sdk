package com.yeepay.yop.sdk.service.common.callback;

import com.yeepay.yop.sdk.internal.DefaultRequest;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.transform.AbstractYopRequestMarshaller;
import com.yeepay.yop.sdk.service.common.request.YopRequest;

/**
 * title: YopCallbackRequest序列化器<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:14
 */
public class YopCallbackRequestMarshaller extends AbstractYopRequestMarshaller {

    private static final YopCallbackRequestMarshaller INSTANCE = new YopCallbackRequestMarshaller();

    public static YopCallbackRequestMarshaller getInstance() {
        return INSTANCE;
    }


    @Override
    protected Request<YopRequest> initRequest(YopRequest request) {
        return new DefaultRequest<>(request);
    }
}
