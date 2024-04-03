/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.yeepay.yop.sdk.invoke.model.Resource;
import com.yeepay.yop.sdk.invoke.model.RouterParams;

/**
 * title: 路由策略<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/3/29
 */
public interface RouterPolicy {

    /**
     * 策略名称
     *
     * @return String
     */
    String name();

    /**
     * 策略逻辑
     *
     * @param params 路由参数
     * @return String 资源
     */
    Resource select(RouterParams params);
}
