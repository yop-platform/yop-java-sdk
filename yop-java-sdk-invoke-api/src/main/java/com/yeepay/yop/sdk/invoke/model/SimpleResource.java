/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

import java.io.Serializable;

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
public class SimpleResource implements Resource, Serializable {

    private static final long serialVersionUID = -1L;

    public SimpleResource(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    /**
     * 资源标识
     */
    private String resourceKey;

    @Override
    public String getResourceKey() {
        return this.resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }
}
