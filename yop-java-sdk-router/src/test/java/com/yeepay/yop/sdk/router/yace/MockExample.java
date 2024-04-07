/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.yace;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.invoke.Router;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.SimpleContext;
import com.yeepay.yop.sdk.router.SimpleUriResourceBusinessLogic;
import com.yeepay.yop.sdk.router.SimpleUriResourceRouter;
import com.yeepay.yop.sdk.router.YopRouterConstants;
import com.yeepay.yop.sdk.router.policy.RouterPolicyFactory;
import com.yeepay.yop.sdk.router.utils.InvokeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * title: 模拟故障示例<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/1/29
 */
public class MockExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockExample.class);

    // 1、处理同类业务的，同一组域名，全局初始化一次即可
    private static final Router<UriResource, Object, SimpleContext> ROUTER = new SimpleUriResourceRouter<>("mock",
            Lists.newArrayList("https://baidu.com", "https://google.com"),
            RouterPolicyFactory.get(YopRouterConstants.ROUTER_POLICY_DEFAULT));

    // 2、测试配置：模拟不同域名的故障率
    private static final Map<String, Integer> MOCK_FAILURE_MAP;
    static {
        MOCK_FAILURE_MAP = new HashMap<>();
        MOCK_FAILURE_MAP.put("https://baidu.com", 100);//1%故障率
        MOCK_FAILURE_MAP.put("https://google.com", 2000);//20%故障率
    }

    // 3、提供业务调用逻辑(简单示例：出参String)
    private static final SimpleUriResourceBusinessLogic<String> BUSINESS_LOGIC =
            new SimpleUriResourceBusinessLogic<String>() {
                @Override
                public String doBusiness(UriResource targetResource, SimpleContext context) {
                    LOGGER.info("请求到：{}", targetResource);
                    return "hello world";
                }
            };

    // 4、mock域名故障示例，20个线程并发，10万笔请求
    public static void main(String[] args) throws InterruptedException {
        // 模拟用户多线程并发请求
        final ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i < 20; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < 500; j++) {
                    try {
                        mockRequest();
                    } catch (Exception e) {
                        FAIL_COUNT.addAndGet(1);
                    } finally {
                        TOTAL_COUNT.addAndGet(1);
                    }
                }
            });
        }
        executorService.shutdown();
        final boolean finished = executorService.awaitTermination(1, TimeUnit.MINUTES);
        LOGGER.info("总量：{}, 失败量：{}, 重试成功量：{}, finished:{}", TOTAL_COUNT.get(), FAIL_COUNT.get(), RETRY_SUCCESS_COUNT.get(), finished);
    }

    private static void mockRequest() {
        InvokeUtils.mockInvoke(BUSINESS_LOGIC, ROUTER, MOCK_FAILURE_MAP, RETRY_SUCCESS_COUNT);
        // 异常分析器，用于分析当笔调用异常原因，是否可重试，是否为域名故障等等
        // 默认实现：基于用户配置的非熔断异常、可重试异常进行分析
        // 可根据需要自行扩展配置，或者调整实现
    }

    // mock结果统计
    private static final AtomicLong TOTAL_COUNT = new AtomicLong();
    private static final AtomicLong FAIL_COUNT = new AtomicLong();
    private static final AtomicLong RETRY_SUCCESS_COUNT = new AtomicLong();
}
