/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk;

import com.yeepay.yop.sdk.auth.credentials.PKICredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.model.yos.YosDownloadInputStream;
import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.rsa.RSAKeyUtils;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * title: benchmark测试<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/1/13
 */
@State(value = Scope.Group)
public class YopClientBenchmarkTest {

    private final YopClient yopClient;

    {
        System.setProperty("yop.sdk.http", "true");
        System.setProperty("yop.sdk.config.env", "qa");
        yopClient = YopClientBuilder.builder().build();
    }

    @Test
    public void runBenchmarks() throws Exception {
        Options options = new OptionsBuilder()
                .include(this.getClass().getName() + ".*")
                .mode(Mode.AverageTime)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .threads(1)
                .measurementIterations(5)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }



    /**
     * GET 普通请求
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Group
    public void testGetCommon() {
        YopRequest request = new YopRequest("/rest/v1.0/test/product-query/query-for-doc", "GET");
        request.addParameter("string0", "dsbzb");
        String appKey = "app_100800191870026";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCkmiIETD1hOQ9ZeCbHkWzmW+TztrwZMOLP5rnIII4iysSJjMDefXslTRyVJCbba4gw40pNPSLMWLK5JgN+KI20/NsKxnVdTGRAbC5pUQZB0B3hdT7oDfQQhprzt0/yzOQP7OPxdcVSE36sFOJSB+KRbyhYrlHk/kPGrehn2tAKBKfsriaBYCw3eF1fN601b6y207c+862zQ48nIEuUSieRttkRqf+zDPjz5awyHV36R/f4dUNrMTkpFAxsajORQ6vQ0C4wYqiL3n0VnnFm8AwCBuunwjhy0nc3TAgergEbatca54r/lMpXUHDffQcWvC3qmwtPOGzcW4liQQ20+yVVAgMBAAECggEARCRW7saNJoGTvCtEipvGpHrohohgyGD3lK/ku1fW38pnoLX3ZmQ2JdQNgCSOPYn/wJVnviAQFt6lZoa5LXImcAW5vHU6QxyL6Cug7xKO75HzvNFn8HNOVcUTws9htpdh1sHv/5cM0BSn/R9MLj97aU/GOSg5WP52GnsGWnA4bdyfrz7ae5jLkVTqrLQec1Vv6WbJuX6co/geqFQuzpH9E0pfP959tXX6srV/Ljl7AcxAbp/B+p6JO+yegEDNKOJ72zezA2WJm1epmARZJvCm/ylKRCQ1Vn9GrFRAxuoFc66875Jk89dL+qiWL3WNG9au2F5W0hqIXj7euFQ5+ZcjAQKBgQD+bgNA7CGs5E10cs9Hwlzd3lC9z/I7Io63m6R+04m0oU6LEwNIuGkeGveDdwr1u+p9qzo/C9sKxiLu4aMsdZuUXgby0J6CDKOfO850WntkZ4vAwWDZdw1+AhKQpFvziK4s6eR6isMXihMsns5pW1XmiG3F+yvKBY1Et4HO/uPpQQKBgQClnjJUef2/fvHXucvxBKObHuoKEOtKjWpl1yZlYLLAbo78ZuY4lDCuY6ldTLcTraDkxno9cgYe44OcLgpgOy9FF3i3BMNblVg9lDvw06K/qo6NzQWrnRONaKPrVlNB4MmMUxTHkp/JCFurf2dXynrQXHAtdhSiE67A2S4+Yf1DFQKBgQDJwXFBDT6S/CNTgStPixfbNEywh/Jc5EVp4bkqfRKIsxU2gWgsRVfN8LJvhHmjbVVAPASfNAaJX58Z8MFVIxmxKHK4H6hiFiW1wlYvvAR3FQkkW/Qx3g9L6dbQ7hSgZLVBlmOArQKRawNTccbakvvmKC9sPIV32LkyUZZVD5ipQQKBgDhQTjTDNjDC7WC44EuXlDnOjS3XCupSo3b1bSzTcHRWI3BXXLioStVBNflveGDMjQS9KBt0hfhCzCFy1jxaY3xOutEgYXzmjxaBLNB6ZcBeMhKtP9xWkKtohTQkKjMgR+fa6BnkFUi5+tGZhH4pbxEiHVKX7/QcVTNlpX+4ufDxAoGAHD/+mC9g5OrKw+RoMUUuNaTrCbXOmKodcggfu9btgHHVY/cae2TG3g3bNWfTEAINg0IaSgG/T3S6eFhAEEpOWOPyPxmnNsjZDaubX1Hj3MGvW7a+YiquAKP+CPXHr/VQgwG0do68F2rnRxSo4ul2fBIUVchFDjlxtgpMWqOTsxs=")
                , null
                , CertTypeEnum.RSA2048)));
        YopResponse response = yopClient.request(request);
        assert ((Map) response.getResult()).get("id").equals(64);
    }

    /**
     * GET 文件下载
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Group
    public void testGetDownload() {
        try {
            YopRequest request = new YopRequest("/yos/v1.0/std/bill/fundbill/download", "GET");
            request.addParameter("fileId", "30343");
            request.addParameter("merchantNo", "10040040287");
            String appKey = "OPR:10040040287";
            request.getRequestConfig().setAppKey(appKey);
            request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
            request.getRequestConfig().setCredentials(new YopPKICredentials(
                    appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                    "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCA3KP3okRyekl4Z2O3Tjud8EEidvuXlcsBiHGrDo7iGNNHoThiAmy9aPUHG02hbzD/i0vAcv41WfaOLGVavFyWtM+MJkxs8NDWyg+iZsJ6Z1nVyYowm6mJOMVLEahHKn+i7G28bw8WlmQh8OoHE2XSgWv1C3oLJ3NrYjq9e/JqqN1R186CLQr48hxry1SlZUQHqV+kNVbthFqImsXvN5H5GiXwpiH7PksX0DIwGWegJyIIDrRutCPNNbZhTy3MtJw40jpGp+6dV/VwOjXd5+9Bz9cM8QJuiHZ1G7MEZa7Fk/VhTMVYYcb7JkRk/aiHgSrXRt+xhs+KqO/ewWGyQ/NrAgMBAAECggEALLxssCvFV3jcyNAb7idayxn9gtmLvrRiJcYYsCn9sMCtmNB/oob0+UzWxADWkpTnjc/uHIHPfvYcgn8nMLjz694Zr64cGjYASpEbnGXQXm5C4dV2BCoGUX1EASvAH/TBGu65qhFkS5MqcB4TISG617nWoNu7IuQvTbhsbBv7rMs0woYlMoYwLnJZRCZ1Z0zqx0vrZZJXcmF4f2oMPeYS5nfcc//9hgKCNzK4E0VGSAhGzTMKhlPTV4OQRBZ0Em7uDKGNM9LAEix6T8fDAYKsUtfSgRs4uvgoVDZD41RosH+rMRWdmy78of9x7OVF7yyIZrKM1DIDWsf/1k3CZF+eAQKBgQC/VHNvus9OH0BZzvgrfHfrCkyTh9oeskKOURtUQvrAXzyjyDBHYWh5cFGLrZ2N3obPbVxEhgtTF5+4OhddIbn1t/z2D+l1bMxjJ7/1zgu7p9ZcvJxCbg3PST9j2kRE61ofcAzV5CAASDGmAUmbLemnBQQPjYO4ZTSbtI31scFgiQKBgQCsaugQXnYw6parUNsnkJjkGqG3wDUWVULBZ7d7Tg6K445DX/L00UiVmH8y3T9vB6xlzkEcz3lb7Gqy0LqajziTHDOrq2cdrhBKrIzDBKJv2Uw7BgFJSy5IsomVzsNNtnx+nsmr70El7D+lc6UT9gIvtjvAlRUwNM8fnaA9tfSvUwKBgDh5JNmFukrEzqmzfWzYgEzP7WpeXvNQinSXXAmy+3Bsq+lr3VQ0XDH8BRXFWQvW8tOm4+UV66HLB0nJW7wRiGIOvIukhrQyOOHLic1z0+K/13Xn9fdlpI3agOtoMV5mWWOOHzvC6e8rX/wvxRoYoy65PEma65YxySVsVbp/jSSBAoGADosV+At/vLCM9PZlBflUbCky6uTzlWarstSkzWnQIDcBkP1O6QeeL8AlOo0fBKaJDC1RoLR/cBmJBnxEwTTDeOUwFpLmp8I+7Y2QcgK6EOUrYO8ovJ3y0X79y/0czDlQVv3d1Prha+l5lOMkUYK2vH+Kmrv4hZBuyCPrmEj40rECgYA0lSMGRXsjoWXn8vefRDIHPKt4i6Nap9RT+aiCrztTayPL8wa7JPc4Bk5iCJZw+gPJ8rll6FEhjrplp7tU1FxxB4UpFI+tOawnxf2712jXtTtKJdr3BLIXqm/T/nkxKBiEdMmde3feK1Ugo+WAP8PE0f9OoueFFNOB9HfZxyNdCg==")
                    , null
                    , CertTypeEnum.RSA2048)));
            YosDownloadResponse response = yopClient.download(request);
            try (YosDownloadInputStream yosDownloadInputStream = response.getResult()) {
                assert null != yosDownloadInputStream;
                assert IOUtils.toString(response.getResult(), "UTF-8").length() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * POST form表单
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Group
    public void testPostForm() {
        YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");
        String appKey = "app_100800095600031";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDJvpvNBByNR/i8Uys1uSJJd9ly3lXBQQxcQIiC+sVDtN8Ejo/g5/k9RGoDplXKnzDQ9pNWpcY9GnYgIbFMKIwUqjGaKCTC4/fGZvt4ugqJaDQKeVQsOIs+475DoCi0X7yC0lk8Z8o+C6HqWghq4aiap1WUYameBy2hPxinX0uWisocZ7np/s01z5jE9afv4Agbq/RLv14YQ0LDVFrsKHjNo4XYe6IR3Wynt28WMa9Gs0Y6WXKhgH58KvksrJX0+TztRbbHnCt3DZ74seaxn+bo9UL8K0Q2T3d02qK0bGBIoZkTLWjzaVVacs7xQ0nXU33BaQ600sXi4y54o/HWlUaBAgMBAAECggEAYoGvkWtwhYue6FWzw4eiNj1O78egF7yrTGA2R74qk+S9AHybxDWAfWnqWd3eBhG0xFOhna1UHoHNK+NHrugdffmcPqlbSc4GLdoa79fnTTCUOIkFkJILa6nIPTz2oxwb78TFzbjgB2umo8dSVN3adak/IDSPnZnjrdghMZhWUCqWllI8/33IeWRu3JUeLSeuGvlU8xF5j1ALXXIyleNjep4HPC/+NNE20kWRQS70ffagYG7NuZA0OQSam1n70+2VNmGYoGSd5LcAlUy4/U7Jh525Wx0vjovoR8AGrAUuTsc/blg1fvYusr3Z7stS7vmPTWcfCYVJ49gOHE4YGfb0AQKBgQDj6b+XdVVjY6XcyVFKCbarlJgbhh9rjGECx2DAlnSmQ+67+IQNpAg8txZ3NykrkjXMZIIOrEugsBp86jrjRI7lF4eI2nWqpe4T7ZNEOKMRRXxT1ediYDLr1PAszx/l0/P4Ro3xubtOjOtzx+xKcGASb34c0Hft99uzOYhmVpvErwKBgQDim0pItsBjv18sGlSJZqBweXM1Frmz0fNxy9fTnTTmRA/o5atWLrvACkH2yxBG7gBOIX71CvurCQy+kFwCMZ6//sbDahm3hrdPCoBP9dp5z7POcFMoJHvZzaxkpbdGlYegXt2km6JltRu8FZX1uEJH77C7vi0ewcwzqN2viAvTzwKBgAi2IY2ffYEMCQX0Z/gFgQbz6hB7Qu4wcnDRwB/8YD8Or6xdpmaDE5GGigRKhndU4luKp/H5ofZlZM3Lgi63qyKUkKipeP/p0bzPQubDp2/8kPD/ZxW6iZe8DuYXkKePP28I+1n2+HLbLhDB3oVF4FY0DsT5LuxYofwqwczvmIqfAoGBAIno8muQdUP/et9vYtWAVNI+x8OeggQTGXK/GSnbeg9NitU1uXGo3XDBjWWyLcTNIfhq4EYnmgR8bHophyV6p1+3oaXaE66i2TrMbEy9lmod4xMXPzSmB44FYw6Z4BGf/Tu3oHKGmW4Gq8tq46n4qrX3BPstgW4/iZRDCC/Ev1X3AoGAZjYOhhnRbxMo/ZuCuTdgqkKk/iOiZRByPhdntvXoGuu3eWsf6LNImDm7LJdLj/nSZhhh1JnKNs3bvvzCjNv0OnxcHTqLufCpTzsh7fOTCAH1YN8qcAdGLUaKDq9w/WnfOsezViCZSwvrSybV0nND+sYEoc+DPluG97isqHjq9ys=")
                , null
                , CertTypeEnum.RSA2048)));
        final String paramString = "你好";
        request.addParameter("string", paramString);
        YopResponse resp = yopClient.request(request);
        assert ((Map) ((Map) resp.getResult()).get("testDTO")).get("string").equals(paramString);
    }

    /**
     * POST json格式
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Group
    public void testPostJson() {
        YopRequest request = new YopRequest("/rest/v1.0/test-wdc/test/http-json", "POST");
        String appKey = "app_100800095600031";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDJvpvNBByNR/i8Uys1uSJJd9ly3lXBQQxcQIiC+sVDtN8Ejo/g5/k9RGoDplXKnzDQ9pNWpcY9GnYgIbFMKIwUqjGaKCTC4/fGZvt4ugqJaDQKeVQsOIs+475DoCi0X7yC0lk8Z8o+C6HqWghq4aiap1WUYameBy2hPxinX0uWisocZ7np/s01z5jE9afv4Agbq/RLv14YQ0LDVFrsKHjNo4XYe6IR3Wynt28WMa9Gs0Y6WXKhgH58KvksrJX0+TztRbbHnCt3DZ74seaxn+bo9UL8K0Q2T3d02qK0bGBIoZkTLWjzaVVacs7xQ0nXU33BaQ600sXi4y54o/HWlUaBAgMBAAECggEAYoGvkWtwhYue6FWzw4eiNj1O78egF7yrTGA2R74qk+S9AHybxDWAfWnqWd3eBhG0xFOhna1UHoHNK+NHrugdffmcPqlbSc4GLdoa79fnTTCUOIkFkJILa6nIPTz2oxwb78TFzbjgB2umo8dSVN3adak/IDSPnZnjrdghMZhWUCqWllI8/33IeWRu3JUeLSeuGvlU8xF5j1ALXXIyleNjep4HPC/+NNE20kWRQS70ffagYG7NuZA0OQSam1n70+2VNmGYoGSd5LcAlUy4/U7Jh525Wx0vjovoR8AGrAUuTsc/blg1fvYusr3Z7stS7vmPTWcfCYVJ49gOHE4YGfb0AQKBgQDj6b+XdVVjY6XcyVFKCbarlJgbhh9rjGECx2DAlnSmQ+67+IQNpAg8txZ3NykrkjXMZIIOrEugsBp86jrjRI7lF4eI2nWqpe4T7ZNEOKMRRXxT1ediYDLr1PAszx/l0/P4Ro3xubtOjOtzx+xKcGASb34c0Hft99uzOYhmVpvErwKBgQDim0pItsBjv18sGlSJZqBweXM1Frmz0fNxy9fTnTTmRA/o5atWLrvACkH2yxBG7gBOIX71CvurCQy+kFwCMZ6//sbDahm3hrdPCoBP9dp5z7POcFMoJHvZzaxkpbdGlYegXt2km6JltRu8FZX1uEJH77C7vi0ewcwzqN2viAvTzwKBgAi2IY2ffYEMCQX0Z/gFgQbz6hB7Qu4wcnDRwB/8YD8Or6xdpmaDE5GGigRKhndU4luKp/H5ofZlZM3Lgi63qyKUkKipeP/p0bzPQubDp2/8kPD/ZxW6iZe8DuYXkKePP28I+1n2+HLbLhDB3oVF4FY0DsT5LuxYofwqwczvmIqfAoGBAIno8muQdUP/et9vYtWAVNI+x8OeggQTGXK/GSnbeg9NitU1uXGo3XDBjWWyLcTNIfhq4EYnmgR8bHophyV6p1+3oaXaE66i2TrMbEy9lmod4xMXPzSmB44FYw6Z4BGf/Tu3oHKGmW4Gq8tq46n4qrX3BPstgW4/iZRDCC/Ev1X3AoGAZjYOhhnRbxMo/ZuCuTdgqkKk/iOiZRByPhdntvXoGuu3eWsf6LNImDm7LJdLj/nSZhhh1JnKNs3bvvzCjNv0OnxcHTqLufCpTzsh7fOTCAH1YN8qcAdGLUaKDq9w/WnfOsezViCZSwvrSybV0nND+sYEoc+DPluG97isqHjq9ys=")
                , null
                , CertTypeEnum.RSA2048)));
        Map<Object, Object> obj = new HashMap<>();
        Map<Object, Object> arg0 = new HashMap<>();
        final String paramString = "你好";
        arg0.put("string", paramString);
        obj.put("arg0", arg0);
        request.setContent(JsonUtils.toJsonString(obj));
        YopResponse resp = yopClient.request(request);
        assert ((Map) ((Map) resp.getResult()).get("testDTO")).get("string").equals(paramString);
    }

    /**
     * POST 文件上传
     */
    @Benchmark
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Group
    public void testPostUpload() throws Exception {
        YopRequest request = new YopRequest("/rest/v1.0/file/upload", "POST");
        String appKey = "app_100400394480007";
        request.getRequestConfig().setAppKey(appKey);
        request.getRequestConfig().setSecurityReq("YOP-RSA2048-SHA256");
        request.getRequestConfig().setCredentials(new YopPKICredentials(
                appKey, new PKICredentialsItem(RSAKeyUtils.string2PrivateKey(
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCBNpSF9EonuJIGGroquixMHf1dZqqfdf+Y5/9mQF2oNWDd7sdSIszPs7VV3FrMxJzEuER5SSgMhUY2wbnGoWjUqWu4NwRn+WPSsAgmKOl3WimSSaa857BO/VQEZb8HlOcgixDAoBc7Zaao9YniL7wBS7X3GtQ4efUYa/vmKMGM7cKXzuXM+2PxEfq43iSwfluoxhc3kDgv7t0DUciBm4TiSrIgRHCb0VlCtfuAo13DHdovis/sgtdUtUIuFrw5kSzNomJn47RdFEQrHMnoA1LgDvadd9CEG6N6o8FakC/fZUXYXW/JNWygqT7FI6JMdTZcFgxP1UhsjmE+kkTJ5bqJAgMBAAECggEAOpyngpvth1cR5fL5v6fzsBNqepO3kd3Us2eJUrifw01zQzis8XUXsp+yAeCSz4/gDNwJM3sbz5Ik53G484EELHMticJrHT7jKQ7wo16riJg9gz4lhEsUjsAa/GOq46WHshti3f3AjBDwKHQ4t4EvpubRA+YHnha0Nv/EpAKYyXPmBCnmHM8eSUcqZCPU7JBC/ukv7/iXjT9gMA/Oe6gL/Mzx7mlPjIk7Mq7CH/Fak8pCqKpM0LuY7DBhDCl4PcqgHxGmhjLUwYuOSMnijXRBXg4YkZ/k8FFY8SldIqkDqGT7BlcgNhvUXH5LNgMHBiAXpMMKoPgm3oRPjGtMzxYAAQKBgQDq3pphzj7YiUAgI5FaTLXjcKb3GcKElePFG8GfXGumOOYTVDlhMuQkthHWbBZfAauMVi3EIv05eEhNdsWyBmNWzdT73+48mdP7cm9zCS1ROotHM8/pV/8vwcDFrwnk35opOzln7f0xzlZenkz0thPYJ1gx6fYzmr3C5HWU9bGnAQKBgQCM1o210IjH0d2IpJvwAEJpNULSML57D4Mfw1OMhosEGMg/fdLcyxcGSiTuHcCSsJhqEb4is64KpGfhu1wGR4vSVVmdcFFGQtXHlOvLglNCzDxlAzXme0gv6V3DE1wTvHsrHw0aNIluaxoK9wsPnBseO60vyeCqzq/7Esj5LXZbiQKBgBxFuY3Gdvg35Vk5Dtkw3MBJIkAigLDXHjju82rMhETZGpD/FX0m1CG7LQCDuFmtaMoW4aF3mMXfPczdXETm0fR0CIxdU19GISdmihXt59+cTYG/sepj5lsIVr01Kdq8M+F8uJdTJaRmMy1mntriRBdD/TDc+f8SRH9+Ys0QmlcBAoGAJGSg09WiMrhZXaDjpr36a0NXFAeCgTw97uxDX7G4pINe44E5BtL4DSkFp/5KL92wVOBm2ILDu35GVb9bhUfhqqVhddx7NAO7SEqEL99qcn1iMdwFhpxex/quvuT2yybOURNCCH6A8OZ+IU07L3pwS3yyQQISqzCjquZsxm7oAbkCgYEAl3B1K3m5DxTWmcmjidhyJhWLwBKLh2lgpdtuWoAs6qwfI9ycZmGy3e/drOI86nrqlOyoP6xsV7V9DK1Pu4xCE4h2YXKtNvtYHN2bCFRA5PqbtiSEHYJsV2j33VuMkyXZP7yTO0tPuntI+8xd5vBn7E1hKyF+4F+Ts2N/ZHA0q9g=")
                , null
                , CertTypeEnum.RSA2048)));
        request.addMultiPartFile("_file", getClass().getResourceAsStream("/simplelogger.properties"));
        YosUploadResponse resp = yopClient.upload(request);
        assert resp.getResult() instanceof Map && !((Map) resp.getResult()).get("success_count").equals(1);
    }

}
