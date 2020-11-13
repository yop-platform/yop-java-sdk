package com.yeepay.yop.sdk.auth.req;

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
     * @param operationId operationId
     * @param securityReq 安全需求
     */
    void register(String operationId, String securityReq);

    /**
     * 获取安全需求
     *
     * @param operationId operationId
     * @return 安全需求
     */
    AuthorizationReq getAuthorizationReq(String operationId);
}
