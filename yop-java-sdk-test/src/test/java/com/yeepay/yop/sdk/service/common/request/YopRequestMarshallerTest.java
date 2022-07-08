/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.request;

import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequestMarshaller;
import org.junit.Assert;
import org.junit.Test;

/**
 * title: Yop请求处理类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/12
 */
public class YopRequestMarshallerTest {

    YopRequestMarshaller marshaller = YopRequestMarshaller.getInstance();
    YopCallbackRequestMarshaller callbackRequestMarshaller = YopCallbackRequestMarshaller.getInstance();

    @Test
    public void marshall() {
        String apiUri = "/rest/v1.0/abc/123";
        final Request<YopRequest> marshalled = marshaller.marshall(new YopRequest(apiUri, "POST"));
        Assert.assertEquals(marshalled.getResourcePath(), apiUri);
        Assert.assertEquals(marshalled.getServiceName(), "abc");
    }

    /**
     * 兼容商户通知
     */
    @Test
    public void marshallCallbackRequest() {
        String apiUri = "/";
        final Request<YopRequest> marshalled = callbackRequestMarshaller.marshall(new YopRequest(apiUri, "POST"));
        Assert.assertEquals(marshalled.getResourcePath(), apiUri);
        Assert.assertEquals(marshalled.getServiceName(), "");
    }
}