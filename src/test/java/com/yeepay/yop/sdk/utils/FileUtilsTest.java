/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.junit.Test;

import java.io.InputStream;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021-05-07
 */
public class FileUtilsTest {

    @Test
    public void testLoadClassPathResource() {
        InputStream resourceAsStream = FileUtils.getResourceAsStream("//config/qa/certs/cfca_root.pem");
        assert null != resourceAsStream;
    }

    @Test
    public void testLoadAbsPathResource() {
        InputStream resourceAsStream = FileUtils.getResourceAsStream("/tmp/yop/certs/yop_platform_sm_cert_275550212193.cer");
        assert null != resourceAsStream;
    }
}
