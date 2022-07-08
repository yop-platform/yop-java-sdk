package com.yeepay.yop.sdk.service.common.request;

import com.yeepay.yop.sdk.internal.DefaultRequest;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.transform.AbstractYopRequestMarshaller;
import org.apache.commons.lang3.StringUtils;

/**
 * title: YopRequest序列化器<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:14
 */
public class YopRequestMarshaller extends AbstractYopRequestMarshaller {

    private static final YopRequestMarshaller INSTANCE = new YopRequestMarshaller();

    public static YopRequestMarshaller getInstance() {
        return INSTANCE;
    }

    @Override
    protected Request<YopRequest> initRequest(YopRequest request) {
        String[] pathParts = StringUtils.split(request.getApiUri(), "/");
        Request<YopRequest> internalRequest = new DefaultRequest<YopRequest>(request, pathParts[2]);
        if (StringUtils.equals(pathParts[0], "yos")) {
            internalRequest.assignYos();
        }
        return internalRequest;
    }
}
