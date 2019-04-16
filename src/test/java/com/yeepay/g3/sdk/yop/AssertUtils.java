package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.client.YopResponse;

import static org.junit.Assert.assertTrue;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/1/24 下午4:01
 */
public class AssertUtils {

    public static void assertYopResponse(YopResponse response) {
        if (!response.isSuccess()) {
            System.out.println(response);
        }
        assertTrue(response.isSuccess());
    }

}
