/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.utils.FileUtils;
import com.yeepay.yop.sdk.utils.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;

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
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = FileUtils.getResourceAsStream("config/certs/qa_cfca_root.pem");
            Assert.assertNotNull(resourceAsStream);
        } finally {
            StreamUtils.closeQuietly(resourceAsStream);
        }
    }

    @Test
    public void testLoadAbsPathResource() throws URISyntaxException {
        final String absolutePath = new File(FileUtils.getContextClassLoader()
                .getResource("config/certs/qa_cfca_root.pem").toURI()).getAbsolutePath();
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = FileUtils.getResourceAsStream(absolutePath);
            Assert.assertNotNull(resourceAsStream);
        } finally {
            StreamUtils.closeQuietly(resourceAsStream);
        }
    }
}
