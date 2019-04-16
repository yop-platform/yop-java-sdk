/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop;

import com.TrustAllHttpsCertificates;
import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * 类名称: Auth2Test <br>
 * 类描述: <br>
 *
 * @author haijun.liao
 * @version 1.0.0
 * @since 16/3/17 下午2:49
 */
public class Auth2Test {

    @Before
    public void setUp() throws Exception {
        TrustAllHttpsCertificates.setTrue();
    }

    @Test
    public void testSendSmsProduct() throws Exception {
        YopRequest request = new YopRequest("test","/MXzsRgoE8zDG+DACb/o1Q==");
        request.setSignAlg("SHA1");//具体看api签名算法而定
        request.addParam("bankCardNumber", "6217211602000141798");
        request.addParam("idCardNumber", "500228199109175071");
        request.addParam("name", "廖海军");
        request.addParam("orderIdentifierType", "OUTER_ORDER_ID");
//        request.addParam("orderId", "RBA15090752367384");
        request.addParam("code", "1234");
//        request.addParam("regNo", "110107013382817");
//        request.addParam("corpName", "北京易通富合科技有限公司");

        request.addParam("requestSystem", "auth2-boss");
        request.addParam("requestCustomerId", "wenkang.zhang");
        request.addParam("requestFlowId", System.currentTimeMillis() + "");
        request.addParam("systemCode", "auth2-boss");
//        request.addParam("requestIP", "requestIP");
//        request.addParam("requestIdentification", "89");
//		request.addParam("authType", "BANK_VERIFY_CREDIT4");
        request.addParam("authMethod", "REPOSITORY_FIRST");
        YopResponse response = YopClient.post("/rest/v1.2/auth/authbankcard", request);
        System.out.println(response);
    }
}