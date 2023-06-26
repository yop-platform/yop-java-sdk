/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.digest;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.base.security.digest.YopDigesterFactory;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.utils.Encodes;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * title: 摘要测试<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/7/5
 */
public class YopDigesterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopDigesterTest.class);
    @Test
    public void testSha256() throws Exception {
        sha256();
        multiThreadDigest();
    }

    private void multiThreadDigest() {
        final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 10,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(100),
                new ThreadFactoryBuilder().setNameFormat("test-task-%d").build(), new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < 10; i++) {
            poolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 100; j++) {
                        try {
                            sha256();
                        } catch (UnsupportedEncodingException e) {
                            LOGGER.warn("sha fail");
                        }
                    }
                    LOGGER.info("finish " + Thread.currentThread().getName());
                }
            });
        }
        MoreExecutors.shutdownAndAwaitTermination(poolExecutor, 1, TimeUnit.MINUTES);
    }

    private void sha256() throws UnsupportedEncodingException {
        final String alg = DigestAlgEnum.SHA256.name();
        Assert.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                Encodes.encodeHex(YopDigesterFactory.getDigester(alg)
                        .digest(new ByteArrayInputStream("".getBytes(YopConstants.DEFAULT_ENCODING)), alg)));

        Assert.assertEquals("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3",
                Encodes.encodeHex(YopDigesterFactory.getDigester(alg)
                        .digest(new ByteArrayInputStream("123".getBytes(YopConstants.DEFAULT_ENCODING)), alg)));
    }

    @Test
    public void testSm3() throws Exception {
        final String alg = DigestAlgEnum.SM3.name();
        Assert.assertEquals("1ab21d8355cfa17f8e61194831e81a8f22bec8c728fefb747ed035eb5082aa2b",
                Encodes.encodeHex(YopDigesterFactory.getDigester(alg)
                        .digest(new ByteArrayInputStream("".getBytes(YopConstants.DEFAULT_ENCODING)), alg)));
    }
}
