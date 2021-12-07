/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.request.YopRequestMarshaller;
import org.junit.Test;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/1
 */
public class HttpUtilsTest {

    @Test
    public void testAssert() {
        final YopRequest yopRequest = new YopRequest("/rest/v1.0/abc/a", "POST");
        yopRequest.addParameter("a", "你好 xx");
        yopRequest.addParameter("b", "-_.*");
        yopRequest.addParameter("c", "z");
        yopRequest.addParameter("d", "Z");
        yopRequest.addParameter("e", "1");
        yopRequest.addParameter("f", "(很好)");
        yopRequest.addParameter("g", "（很好）");
        yopRequest.addParameter("h", "'");
        yopRequest.addParameter("i", "～");
        yopRequest.addParameter("j", "!");
        yopRequest.addParameter("k", "！");
        yopRequest.addParameter("l", "");
        final Request<YopRequest> request = YopRequestMarshaller.getInstance().marshall(yopRequest);

//        final String s1 = HttpUtils.encodeParameters(request, true);
        final String s2 = HttpUtils.encodeParameters(request, true);


//        final String s3 = HttpUtils.encodeParameters(request, false);
        final String s4 = HttpUtils.encodeParameters(request, false);

        assert s2.equals("a=%E4%BD%A0%E5%A5%BD+xx&b=-_.*&c=z&d=Z&e=1&f=%28%E5%BE%88%E5%A5%BD%29&g=%EF%BC%88%E5%BE%88%E5%A5%BD%EF%BC%89&h=%27&i=%EF%BD%9E&j=%21&k=%EF%BC%81&l=");
        assert s4.equals("a=%25E4%25BD%25A0%25E5%25A5%25BD%2Bxx&b=-_.*&c=z&d=Z&e=1&f=%2528%25E5%25BE%2588%25E5%25A5%25BD%2529&g=%25EF%25BC%2588%25E5%25BE%2588%25E5%25A5%25BD%25EF%25BC%2589&h=%2527&i=%25EF%25BD%259E&j=%2521&k=%25EF%25BC%2581&l=");
//        System.out.println(s1);
//        System.out.println(s2);
//        System.out.println(s3);
//        System.out.println(s4);

//        assert s1.equals(s2);
//        assert s3.equals(s4);
    }
}
