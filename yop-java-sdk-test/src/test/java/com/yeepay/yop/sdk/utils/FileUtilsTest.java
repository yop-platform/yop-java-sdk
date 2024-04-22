/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
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
            resourceAsStream = FileUtils.getResourceAsStream("config/certs/yeepay/qa/cfca_root.pem");
            Assert.assertNotNull(resourceAsStream);
        } finally {
            StreamUtils.closeQuietly(resourceAsStream);
        }
    }

    @Test
    public void testLoadAbsPathResource() throws URISyntaxException {
        final String absolutePath = new File(FileUtils.getContextClassLoader()
                .getResource("config/certs/yeepay/qa/cfca_root.pem").toURI()).getAbsolutePath();
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = FileUtils.getResourceAsStream(absolutePath);
            Assert.assertNotNull(resourceAsStream);
        } finally {
            StreamUtils.closeQuietly(resourceAsStream);
        }
    }

    @Test
    public void testFileInputStream() throws Exception {
        final FileInputStream fileInputStream = new FileInputStream(new File(System.getProperty("user.dir") + "/src/test/resources/test.txt"));
        final Field pathField = FileInputStream.class.getDeclaredField("path");
        pathField.setAccessible(true);
        assert (StringUtils.substringAfterLast((String) pathField.get(fileInputStream), "/").equals("test.txt"));
    }
}
