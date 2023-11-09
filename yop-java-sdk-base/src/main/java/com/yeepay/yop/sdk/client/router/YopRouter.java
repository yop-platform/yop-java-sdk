/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.router;

import com.yeepay.yop.sdk.client.ClientExecutionParams;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.Router;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;

import java.net.URI;
import java.util.List;

/**
 * title: yop域名路由<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/7
 */
public class YopRouter<Input extends BaseRequest, OutPut extends BaseResponse>
        implements Router<URI, ClientExecutionParams<Input, OutPut>, ExecutionContext> {

    private final GateWayRouter gateWayRouter;

    public YopRouter(GateWayRouter gateWayRouter) {
        this.gateWayRouter = gateWayRouter;
    }

    @Override
    public URI route(ClientExecutionParams<Input, OutPut> executionParams, ExecutionContext executionContext, Object...args) {
        Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
        return gateWayRouter.route(executionContext.getYopCredentials().getAppKey(), request, (List<URI>) args[0]);
    }
}
