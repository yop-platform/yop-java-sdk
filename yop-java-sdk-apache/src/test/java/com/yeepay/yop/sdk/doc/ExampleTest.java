/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.doc;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;
import com.yeepay.yop.sdk.utils.Sm2Utils;
import org.junit.Test;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/8
 */
public class ExampleTest {

    @Test
    public void testPostFormRsaExample() {
        System.setProperty("yop.sdk.http", "true");//2021-12-08T11:59:16Z,d48782ac-93c1-466e-b417-f7a71e4965f0
        YopClient yopClient = YopClientBuilder.builder().withEndpoint("https://openapi.yeepay.com/yop-center").build();
        YopRequest request = new YopRequest("/rest/v1.0/trade/order", "POST");
        String appKey = "app_100123456789";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+YgO139eaN/Cjd3mTE4ePwqSI1F8ubXojffiwXy+mEiYGR4YscIcPQiYUGb2YpZQHa/Zoz+OyuloBCQBS1C8cva91KJojzUA4ll88vj8JF64G3P6WZh8acoUdNo8WRWfj9TVMMPBtzVcLK2bujrfx/t5Sggi66IK1FthcEtrkN8atA3rLj4OhNbZOzQRadecZDkeVelXU5LvNvBhBwO1cJ2Agr7ezkUaQENau/TSIAKdGJt607daB/MDgQdNrCNc/lUnp9+a8BUNYNCyJQZJKeAyVqFO73c/v3dlRaAUUfoH+hIbmS0g3aSpmxexvka6BFEld16wRG41VSGFhXbkRAgMBAAECggEASC9/uqkp5ZaKTmDRnvuLre2eVyc3A7KM2gI8lhsxROWit0TNUfJEs3tgVsS/x64YZ4v+/RS+ABl6YOQZ1E4RovMlIOYJM8PyMsKJT83OttLcsEuA2GPWLT/4yu/R5x7f2mYyFDaGIwv1kg2d1JwWkNITV/Nn/f6E+Ma1uIuJpXf9CVxIokfWFMstGNAGw/871V1qKAIDRsWTN4gTT4aRK/FPvQNzHv4nSEtlYdAYE8r53MaAZfigfFSOGowPFegyktQJXfmAUOhZbRhRZGQqcwU/1M5/TKu1cJECM/N/1ttjMlPNamQmONawq8dqfpK7a45YyWgyaadN2flA4/nWdQKBgQDWwrQsxnoVcoL88fFZYwol/5RYG+eA9zMffCi39KsKBU6ePbLlORYd2D/f2nDno6Uz2tFnUoRLvKy3ZINuIdN/jgD4ob69tk7XIKQSzh9Tv2485P8PasublywgdG9LnYk8qbF1VDsOkgecSSh7xG8Rz/U9p9kI5/wt3OOc0brjKwKBgQDi8PDtFziZNVSC58BcaWpAfZyDwB8X56BtNz1890zVOvF8ali6GUwgZkcH8KsQXhu+1YkmnC/YS6H0s+ZE4CIP6FGw5Z8988UB2i+oB0BMK8l8WDFOgPyW2n9l6502Qx1tqD3alekcksFsIlUgP9sVc5vtAKUPtNgguhRcP6mmswKBgQDCkkSLDIcvR0BFyy3OvlxDcPsFmMJ1pYE71VFO2Ozdd1FzLJMX+lB/WZ0FQvNn6muSP33ZDnmt5JLW1Mn+zcbAmfdnS6N0XeewIHKGVxkq1xUZNp+faDJwFNZ10QfEikX8IAIXOukGmmcqwV1cROwcRzz5T0jjOMrRAn91ZM7dYQKBgC91JVzfU0WuwlqRrkdlAAQ2gGmI3re4B3NvbttYN+gLaH6VGrLoIWRRHx+I86z7kR/KNeEuHk9EGb07dbcHi/f5pEOy8ScaeCNYBklEIu0K5xqqsrzw+mFtleCxcfHr/RZ2bWDtoo8IHYzIbTbOQ7lrsLrSPLJZJi1J3IIiCg9DAoGAOxT0qqTUADmSvwnzyQAYJ5sFI36eMcKqwkBuqb7ZQiLFNv1WZROrkeGin2ishntFKsIUtrpeikPjNP2AX6X0UuSQsUNNWx1zYpSlNUyGtGueYhmmP+7plPN5BhuJ3Ba6IYC/uI/l1tJP3S4e/xa/rCcNrf36RzK+PLLPq/uPAaY=")
                , null
                , CertTypeEnum.RSA2048)));
        request.addParameter("parentMerchantNo", "1234321");
        request.addParameter("orderId", "1234321");
        request.addParameter("orderAmount", "100.05");
        request.addParameter("notifyUrl", "https://xxx.com/notify");
        assertTheRequest(yopClient, request);
    }

    private void assertTheRequest(YopClient yopClient, YopRequest request) {
        try {
            YopResponse resp = yopClient.request(request);
        } catch (Exception e) {
            assert e instanceof YopServiceException && ((YopServiceException) e).getSubErrorCode().equals("isv.app.not-exists");
        }
    }

    @Test
    public void testPostFormSm2Example() {
        System.setProperty("yop.sdk.http", "true");
        YopClient yopClient = YopClientBuilder.builder().withEndpoint("https://openapi.yeepay.com/yop-center").build();
        YopRequest request = new YopRequest("/rest/v1.0/trade/order", "POST");
        String appKey = "app_100123456789";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-SM2-SM3");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(Sm2Utils.string2PrivateKey(
                "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQg/WsUu5NQDTDJjjaXWLlNfBNZhamXAqCLcyLPSHDSD4qgCgYIKoEcz1UBgi2hRANCAAQUC8TdvSHnCXGlQzm62w+sqHK8wt/ZDXmuhyU4qOEJ8jRMiTzQWoX8BC0fB7ggzWIobHrJouBgnEm3AxVhShpZ")
                , null
                , CertTypeEnum.SM2)));
        request.addParameter("parentMerchantNo", "1234321");
        request.addParameter("orderId", "1234321");
        request.addParameter("orderAmount", "100.05");
        request.addParameter("notifyUrl", "https://xxx.com/notify");
        assertTheRequest(yopClient, request);
    }

    @Test
    public void singleUpload() {
        System.setProperty("yop.sdk.http", "true");//2021-12-08T11:59:16Z,d48782ac-93c1-466e-b417-f7a71e4965f0
        YopClient yopClient = YopClientBuilder.builder().withYosEndpoint("https://yos.yeepay.com/yop-center").build();
        YopRequest request = new YopRequest("/yos/v1.0/sys/merchant/qual/upload", "POST");
        String appKey = "app_100123456789";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+YgO139eaN/Cjd3mTE4ePwqSI1F8ubXojffiwXy+mEiYGR4YscIcPQiYUGb2YpZQHa/Zoz+OyuloBCQBS1C8cva91KJojzUA4ll88vj8JF64G3P6WZh8acoUdNo8WRWfj9TVMMPBtzVcLK2bujrfx/t5Sggi66IK1FthcEtrkN8atA3rLj4OhNbZOzQRadecZDkeVelXU5LvNvBhBwO1cJ2Agr7ezkUaQENau/TSIAKdGJt607daB/MDgQdNrCNc/lUnp9+a8BUNYNCyJQZJKeAyVqFO73c/v3dlRaAUUfoH+hIbmS0g3aSpmxexvka6BFEld16wRG41VSGFhXbkRAgMBAAECggEASC9/uqkp5ZaKTmDRnvuLre2eVyc3A7KM2gI8lhsxROWit0TNUfJEs3tgVsS/x64YZ4v+/RS+ABl6YOQZ1E4RovMlIOYJM8PyMsKJT83OttLcsEuA2GPWLT/4yu/R5x7f2mYyFDaGIwv1kg2d1JwWkNITV/Nn/f6E+Ma1uIuJpXf9CVxIokfWFMstGNAGw/871V1qKAIDRsWTN4gTT4aRK/FPvQNzHv4nSEtlYdAYE8r53MaAZfigfFSOGowPFegyktQJXfmAUOhZbRhRZGQqcwU/1M5/TKu1cJECM/N/1ttjMlPNamQmONawq8dqfpK7a45YyWgyaadN2flA4/nWdQKBgQDWwrQsxnoVcoL88fFZYwol/5RYG+eA9zMffCi39KsKBU6ePbLlORYd2D/f2nDno6Uz2tFnUoRLvKy3ZINuIdN/jgD4ob69tk7XIKQSzh9Tv2485P8PasublywgdG9LnYk8qbF1VDsOkgecSSh7xG8Rz/U9p9kI5/wt3OOc0brjKwKBgQDi8PDtFziZNVSC58BcaWpAfZyDwB8X56BtNz1890zVOvF8ali6GUwgZkcH8KsQXhu+1YkmnC/YS6H0s+ZE4CIP6FGw5Z8988UB2i+oB0BMK8l8WDFOgPyW2n9l6502Qx1tqD3alekcksFsIlUgP9sVc5vtAKUPtNgguhRcP6mmswKBgQDCkkSLDIcvR0BFyy3OvlxDcPsFmMJ1pYE71VFO2Ozdd1FzLJMX+lB/WZ0FQvNn6muSP33ZDnmt5JLW1Mn+zcbAmfdnS6N0XeewIHKGVxkq1xUZNp+faDJwFNZ10QfEikX8IAIXOukGmmcqwV1cROwcRzz5T0jjOMrRAn91ZM7dYQKBgC91JVzfU0WuwlqRrkdlAAQ2gGmI3re4B3NvbttYN+gLaH6VGrLoIWRRHx+I86z7kR/KNeEuHk9EGb07dbcHi/f5pEOy8ScaeCNYBklEIu0K5xqqsrzw+mFtleCxcfHr/RZ2bWDtoo8IHYzIbTbOQ7lrsLrSPLJZJi1J3IIiCg9DAoGAOxT0qqTUADmSvwnzyQAYJ5sFI36eMcKqwkBuqb7ZQiLFNv1WZROrkeGin2ishntFKsIUtrpeikPjNP2AX6X0UuSQsUNNWx1zYpSlNUyGtGueYhmmP+7plPN5BhuJ3Ba6IYC/uI/l1tJP3S4e/xa/rCcNrf36RzK+PLLPq/uPAaY=")
                , null
                , CertTypeEnum.RSA2048)));
        request.addMultiPartFile("merQual", getClass().getResourceAsStream("/test.txt"));
        final YosUploadResponse upload = yopClient.upload(request);
        assert null != upload;
    }
}
