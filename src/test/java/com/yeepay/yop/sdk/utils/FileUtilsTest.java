/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.junit.Test;

import java.io.File;
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
    public void testLoadClassPathResource() throws Exception {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = FileUtils.getResourceAsStream("config/certs/qa_cfca_root.pem");
            assert null != resourceAsStream;
        } finally {
            if (null != resourceAsStream) {
                resourceAsStream.close();
            }
        }
    }

    @Test
    public void testLoadAbsPathResource() throws Exception {
        final String absolutePath = new File(FileUtils.getContextClassLoader()
                .getResource("config/certs/qa_cfca_root.pem").toURI()).getAbsolutePath();
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = FileUtils.getResourceAsStream(absolutePath);
            assert null != resourceAsStream;
        } finally {
            if (null != resourceAsStream) {
                resourceAsStream.close();
            }
        }
    }
}
