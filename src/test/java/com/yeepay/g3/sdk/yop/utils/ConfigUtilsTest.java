/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.config.support.ConfigUtils;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/7/28
 */
public class ConfigUtilsTest {

    @Test
    public void listConfig() throws IOException, URISyntaxException {
        System.out.println(ConfigUtils.listFiles("config"));
    }
}
