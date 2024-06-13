/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.invoke.model.UriResource;

/**
 * title: 封装商户业务逻辑<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/5
 */
public interface SimpleUriResourceBusinessLogic<Output> {

    /**
     * 业务逻辑
     *
     * @param targetResource 路由目标
     * @param context        上下文信息
     * @return 业务结果
     */
    Output doBusiness(UriResource targetResource, SimpleContext context);
}
