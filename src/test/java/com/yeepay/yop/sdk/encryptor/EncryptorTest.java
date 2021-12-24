/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.encryptor;

import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.encryptor.auth.credentials.provider.MockEncryptorCredentialsProvider;
import com.yeepay.yop.sdk.encryptor.signer.process.MockEncryptorSignProcessor;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.YopClientImpl;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
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
public class EncryptorTest {

    @Test
    public void test() {
        System.setProperty("yop.sdk.config.env", "qa_single_default");
        YopSignProcessorFactory.registerSignProcessor(CertTypeEnum.SM2.name(), new MockEncryptorSignProcessor());
        YopClientImpl yopClient = YopClientBuilder.builder()
                .withCredentialsProvider(new MockEncryptorCredentialsProvider())
                .build();

        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");

        YopResponse response = yopClient.request(request);
        System.out.println(response);
    }
}
