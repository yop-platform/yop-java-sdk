/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.digest;

import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.base.security.digest.YopDigesterFactory;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.utils.Encodes;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;

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

    @Test
    public void testSha256() throws Exception {
        final String alg = DigestAlgEnum.SHA256.name();
        Assert.assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                Encodes.encodeHex(YopDigesterFactory.getDigester(alg)
                        .digest(new ByteArrayInputStream("".getBytes(YopConstants.DEFAULT_ENCODING)), alg)));
    }

    @Test
    public void testSm3() throws Exception {
        final String alg = DigestAlgEnum.SM3.name();
        Assert.assertEquals("1ab21d8355cfa17f8e61194831e81a8f22bec8c728fefb747ed035eb5082aa2b",
                Encodes.encodeHex(YopDigesterFactory.getDigester(alg)
                        .digest(new ByteArrayInputStream("".getBytes(YopConstants.DEFAULT_ENCODING)), alg)));
    }
}
