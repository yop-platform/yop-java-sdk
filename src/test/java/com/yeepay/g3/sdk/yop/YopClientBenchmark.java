package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.core.yop.utils.test.benchmark.BenchmarkTask;
import com.yeepay.g3.core.yop.utils.test.benchmark.ConcurrentBenchmark;
import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 2018/3/1 下午12:34
 */
public class YopClientBenchmark extends ConcurrentBenchmark {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopClientBenchmark.class);

    private static final int DEFAULT_THREAD_COUNT = 40;
    private static final long DEFAULT_TOTAL_COUNT = 10000;

    public YopClientBenchmark(int defaultThreadCount, long defaultTotalCount) {
        super(defaultThreadCount, defaultTotalCount);
    }

    public static void main(String[] args) throws Exception {
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_local.json");
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_pro.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_dev.json");

        AppSdkConfigProviderRegistry.getProvider().getDefaultConfig();

        YopClientBenchmark benchmark = new YopClientBenchmark(DEFAULT_THREAD_COUNT, DEFAULT_TOTAL_COUNT);
        benchmark.execute();
    }

    public class InvokeTask extends BenchmarkTask {
        @Override
        protected void execute(int requestSequence) {
            try {
//                YopRequest request = new YopRequest();
//                request.addParam("username", "siqi");
//                request.addParam("password", "qisi");
//                YopRsaClient.post("/rest/v1.0/router/open-pay-report/query", request);

                YopRequest request = new YopRequest();
                request.addParam("trxRequestNo", "111");
                request.addParam("remitRequestNo", "Remit1534859751218");
                YopResponse response = YopClient.post("/rest/v1.0/insordering-interface/car-insordering-interface/create-policy", request);
//                Thread.sleep(2500);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    @Override
    protected BenchmarkTask createTask() {
        return new InvokeTask();
    }

}
