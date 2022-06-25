/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.com.yeepay.yop.sdk;

import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfigProvider;
import org.junit.After;
import org.junit.Before;

/**
 * title: 测试基类，环境设置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/12
 */
public class BaseTest {

    @Before
    public void setUp() {
        clear();
    }

    @After
    public void teardown() {
        clear();
    }

    private void clear() {
        System.clearProperty(YopFileSdkConfigProvider.SDK_CONFIG_DIR_PROPERTY_KEY);
        System.clearProperty(YopFileSdkConfigProvider.SDK_CONFIG_ENV_PROPERTY_KEY);
        System.clearProperty(YopFileSdkConfigProvider.SDK_CONFIG_FILE_PROPERTY_KEY);
    }

}
