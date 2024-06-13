/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

import java.util.List;

/**
 * title: 简单路由参数<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/3/29
 */
public class SimpleRouterParams implements RouterParams {

    /**
     * 资源分组
     */
    private String resourceGroup;

    /**
     * 可用资源列表
     */
    private List<String> availableResources;

    /**
     * 已调用资源列表
     */
    private List<String> invokedResources;

    public SimpleRouterParams() {
    }

    public SimpleRouterParams(String resourceGroup, List<String> availableResources, List<String> invokedResources) {
        this.resourceGroup = resourceGroup;
        this.availableResources = availableResources;
        this.invokedResources = invokedResources;
    }

    @Override
    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    @Override
    public List<String> getAvailableResources() {
        return availableResources;
    }

    public void setAvailableResources(List<String> availableResources) {
        this.availableResources = availableResources;
    }

    @Override
    public List<String> getInvokedResources() {
        return invokedResources;
    }

    public void setInvokedResources(List<String> invokedResources) {
        this.invokedResources = invokedResources;
    }
}
