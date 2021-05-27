/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.yos;

import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 5/27/21
 */
public class YosTest {

    private static YopClient yopClient;

    static {
//        System.setProperty("yop.sdk.config.file", "file:///Users/yp-21022/Develop/Projects/Yeepay/yop-sdks/sdk/src/main/resources/config/yop_sdk_config_default_test.json");
//        System.setProperty("yop.sdk.config.env", "qa");
        yopClient = YopClientBuilder.builder().build();
    }

    @Test
    public void testYosDownload() throws Exception {
        ExecutorService thread = Executors.newScheduledThreadPool(120,new ThreadFactory() {
            int i = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "TestThreadPool-" + (i++));
            }
        });
        for (int i = 0; i < 100; i++) {
            final int suffix = i;
            thread.execute(() -> remoteRequest("子线程(" + suffix + ")", true));
            // 频度控制
            Thread.sleep(1000);
        }
        thread.shutdown();
        thread.awaitTermination(10, TimeUnit.MINUTES);

        remoteRequest("主线程", true);
    }

    private void remoteRequest(String s, boolean closeResp) {
        System.out.println(s + "开始执行");
        try {
            YopRequest requset = new YopRequest("/yos/v1.0/balance/yop-simple-remit/download-electronic-receipt", "GET");
            requset.addParameter("batchNo", "000000005499580");
            requset.addParameter("orderId", "YB654db6376fd04045a6abd82f055f6e04");
            YosDownloadResponse response = yopClient.download(requset);
            if (closeResp) {
                try {
                    IOUtils.toString(response.getResult(), "UTF-8");
//                    response.getResult().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(s + "结束执行");
    }
}
