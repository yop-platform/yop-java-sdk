package com.yeepay.yop.sdk.auth.req;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * title: AuthorizationReq注册中心<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 17:50
 */
public class DefaultAuthorizationReqRegistry implements AuthorizationReqRegistry {

    private final Map<String, List<AuthorizationReq>> authorizationReqs;

    public DefaultAuthorizationReqRegistry() {
        authorizationReqs = new HashMap<String, List<AuthorizationReq>>();
    }

    @Override
    public void register(String operationId, List<String> securityReqs) {
        List<AuthorizationReq> authorizationReqList = new ArrayList<>(securityReqs.size());
        for (String securityReq : securityReqs) {
            authorizationReqList.add(AuthorizationReqSupport.getAuthorizationReq(securityReq));
        }
        authorizationReqs.put(operationId, authorizationReqList);
    }

    @Override
    public List<AuthorizationReq> getAuthorizationReq(String operationId) {
        return authorizationReqs.get(operationId);
    }
}
