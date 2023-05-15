package com.yeepay.yop.sdk.model.transform;

import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseRequest;


public interface RequestMarshaller<Req extends BaseRequest> {

    Request<Req> marshall(Req request);

}