package com.yeepay.yop.sdk.service.common.auth;

import com.yeepay.g3.core.yop.sdk.sample.auth.AuthorizationReq;
import com.yeepay.g3.core.yop.sdk.sample.auth.AuthorizationReqRegistry;
import com.yeepay.g3.core.yop.sdk.sample.auth.AuthorizationReqSupport;

/**
 * title: Mock认证注册中心<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2020<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 17:39
 */
public class MockAuthorizationReqRegistry implements AuthorizationReqRegistry {

    @Override
    public void register(String operationId, String securityReq) {
        //do nothing
    }

    @Override
    public AuthorizationReq getAuthorizationReq(String operationId) {
        return AuthorizationReqSupport.getAuthorizationReq("YOP-RSA2048-SHA256");
    }
}
