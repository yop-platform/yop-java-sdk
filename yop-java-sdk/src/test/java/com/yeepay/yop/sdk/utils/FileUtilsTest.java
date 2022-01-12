/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
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
        try (InputStream resourceAsStream = FileUtils.getResourceAsStream("config/certs/qa_cfca_root.pem")) {
            assert null != resourceAsStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadAbsPathResource() throws URISyntaxException {
        final String absolutePath = new File(FileUtils.getContextClassLoader()
                .getResource("config/certs/qa_cfca_root.pem").toURI()).getAbsolutePath();
        try (InputStream resourceAsStream = FileUtils.getResourceAsStream(absolutePath)) {
            assert null != resourceAsStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}