/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.config;

import java.util.List;
import java.util.Set;

/**
 * title: 熔断配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public interface CircuitBreakerConfig<Rule> {

    /**
     * 是否启用
     *
     * @return true:启用，false:禁用
     */
    boolean isEnable();

    /**
     * 加载熔断规则
     *
     * @return 熔断规则
     */
    List<Rule> getRules();

    /**
     * 不计入熔断的异常列表
     *
     * @return 异常列表
     */
    Set<String> getExcludeExceptions();

}
