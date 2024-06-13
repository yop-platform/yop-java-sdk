/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.invoke.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: 通用资源调用示例<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/10
 */
public class SimpleExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExample.class);

    // 1、初始化路由客户端
    // 1.1、构造每一个client时，请确保传入的一组域名之间关系对等，均能处理同类业务
    // 1.2、可以根据业务分类，合理拆分client，每个client，使用单例模式
    // 1.3、发生网络故障时，使用同一个client实例发出的多个请求，共享同一套路由切换逻辑
    private static final ResourceRouteClient ROUTE_CLIENT = new ResourceRouteClient(
            Lists.newArrayList("A", "B", "C"));



    // 2、提供业务调用逻辑(简单示例：出参为String)
    // 2.1、入参为：待请求目标域名targetResource、当前请求上下文信息simpleContext
    // 2.2、根据需要补充自身业务处理逻辑
    // 2.3、根据情况抛出指定异常
    private static final ResourceInvocation<String> INVOKE_LOGIC =
            new ResourceInvocation<String>() {
                @Override
                public String doInvoke(Resource targetResource, SimpleContext context) {
                    LOGGER.info("请求到：{}", targetResource.getResourceKey());
                    // TODO 区分出客户端错误、业务处理错误等非网络故障，抛出指定异常
                    //模拟逻辑：客户端错误(参数校验、转换等)
                    boolean clientError = false;
                    if (clientError) {
                        throw new YopClientException("xxx");
                    }

                    //模拟逻辑：业务处理错误(服务端返回了相关业务错误码)
                    boolean businessError = false;
                    if (businessError) {
                        throw new YopServiceException("xxx");
                    }

                    // 其他错误交给上层自动解析处理
                    return "hello world";
                }
            };

    // 3、调用示例：使用路由客户端，传入业务逻辑，即可完成调用
    public static void main(String[] args) {
        final String output = ROUTE_CLIENT.route(INVOKE_LOGIC);
        assert "hello world".equals(output);
    }

}
