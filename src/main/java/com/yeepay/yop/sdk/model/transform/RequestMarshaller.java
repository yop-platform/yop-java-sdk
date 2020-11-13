package com.yeepay.yop.sdk.model.transform;

import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;


public interface RequestMarshaller<Req extends BaseRequest> {

    Request<Req> marshall(Req request);

}
