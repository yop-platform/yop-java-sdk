/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.invoke.model.Resource;

/**
 * title: 资源调用逻辑<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/8
 */
public interface ResourceInvocation<Output> {

    /**
     * 资源访问逻辑
     *
     * @param resource 目标资源
     * @param context  上下文信息
     * @return 业务结果
     */
    Output doInvoke(Resource resource, SimpleContext context);
}
