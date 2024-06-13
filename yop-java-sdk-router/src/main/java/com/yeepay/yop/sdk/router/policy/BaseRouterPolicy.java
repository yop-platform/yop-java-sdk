/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.policy;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.invoke.RouterPolicy;
import com.yeepay.yop.sdk.invoke.model.RouterParams;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/1
 */
public abstract class BaseRouterPolicy implements RouterPolicy {

    @Override
    public String name() {
        return this.getClass().getCanonicalName();
    }

    protected void validateParams(RouterParams params) {
        if (null == params.getResourceGroup()) {
            throw new YopClientException("ConfigProblem, resource group is not specified");
        }
        if (null == params.getAvailableResources() || params.getAvailableResources().isEmpty()) {
            throw new YopClientException("ConfigProblem, no available resource is specified");
        }
        if (null != params.getInvokedResources()) {
            for (String invokedResource : params.getInvokedResources()) {
                if (!params.getAvailableResources().contains(invokedResource)) {
                    throw new YopClientException("ConfigProblem, invoked resource is not belong to the available resource");
                }
            }
        }
    }


}
