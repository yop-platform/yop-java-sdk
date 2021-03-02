package com.yeepay.yop.sdk.auth.req;

import java.util.List;

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
public interface AuthorizationReqRegistry {

    /**
     * 注册安全需求
     *
     * @param operationId  operationId
     * @param securityReqs 安全需求
     */
    void register(String operationId, String securityReqs);

    /**
     * 获取安全需求
     *
     * @param operationId operationId
     * @return 安全需求
     */
    List<AuthorizationReq> getAuthorizationReq(String operationId);
}
