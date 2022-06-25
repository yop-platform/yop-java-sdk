/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.com.yeepay.yop.sdk.encryptor;

import com.com.yeepay.yop.sdk.BaseTest;
import com.com.yeepay.yop.sdk.encryptor.auth.credentials.provider.MockEncryptorCredentialsProvider;
import com.com.yeepay.yop.sdk.encryptor.signer.process.MockEncryptorSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.YopClientImpl;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author qi.zhang-4
 * @version 1.0.0
 * @since 2021/12/23 10:47 上午
 */
@Ignore
public class EncryptorTest extends BaseTest {

    @Test
    public void test() {
        System.setProperty("yop.sdk.config.env", "qa_single_default");
        final YopSignProcessor oldSigner = YopSignProcessorFactory.getSignProcessor(CertTypeEnum.SM2.name());
        try {
            YopSignProcessorFactory.registerSignProcessor(CertTypeEnum.SM2.name(), new MockEncryptorSignProcessor());
            YopClientImpl yopClient = YopClientBuilder.builder()
                    .withCredentialsProvider(new MockEncryptorCredentialsProvider())
                    .build();

            YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

            YopResponse response = yopClient.request(request);
            Assert.assertNotNull(response);
        } finally {
            YopSignProcessorFactory.registerSignProcessor(CertTypeEnum.SM2.name(), oldSigner);
        }

    }
}
