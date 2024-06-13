/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

import java.util.List;

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
public interface RouterParams {

    /**
     * 资源分组
     *
     * @return String
     */
    String getResourceGroup();

    /**
     * 可用资源列表
     *
     * @return List<String>
     */
    List<String> getAvailableResources();

    /**
     * 已调用资源列表(当笔重试)，按调用时间正序
     *
     * @return List<String>
     */
    List<String> getInvokedResources();
}
