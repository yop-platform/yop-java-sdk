/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2021/12/22 22:30
 */
public class YopSignProcessorFactoryTest {

    @Test
    public void getSignProcessor() {
        YopSignProcessor yopSignProcessor = YopSignProcessorFactory.getSignProcessor("SM2");
        assertNotNull(yopSignProcessor);
    }

}
