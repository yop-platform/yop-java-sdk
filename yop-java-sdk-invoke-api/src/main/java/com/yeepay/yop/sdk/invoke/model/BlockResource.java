/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

/**
 * title: 熔断资源<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/1
 */
public class BlockResource extends SimpleResource {

    private static final long serialVersionUID = -1L;

    /**
     * 熔断资源序列号
     */
    private long blockSequence;

    public BlockResource(String resourceKey, long blockSequence) {
        super(resourceKey);
        this.blockSequence = blockSequence;
    }

    public long getBlockSequence() {
        return blockSequence;
    }
}
