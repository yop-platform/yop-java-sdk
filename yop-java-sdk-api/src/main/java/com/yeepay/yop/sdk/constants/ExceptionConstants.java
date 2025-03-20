/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.constants;

/**
 * title: YOP-SDK异常码定义<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2025/2/24
 */
public interface ExceptionConstants {

    /**
     * 错误描述：SDK项目运行时依赖配置异常
     * 解决方案：检查pom.xml文件与类加载环境，确保按官方文档引入相关依赖
     */
    String SDK_CONFIG_RUNTIME_DEPENDENCY = "sdk.config.runtime.dependency";

    /**
     * 错误描述：SDK配置项格式错误
     * 解决方案：检查yop_sdk_config_xxx.json文件与YopFileSdkConfig配置类的属性，确保类型与格式正确
     */
    String SDK_CONFIG_PARAM_FORMAT = "sdk.config.param.format";

    /**
     * 错误描述：SDK请求参数格式错误
     * 解决方案：检查接口文档的参数描述与xxxRequest设置的实际参数，确保类型与格式正确
     */
    String SDK_INVOKE_REQUEST_PARAM_FORMAT_PREFIX = "sdk.invoke.check.request-param.format";

    /**
     * 错误描述：SDK请求域名解析异常
     * 解决方案：
     * 1.检查客户端代理与服务端请求域名，确认网络配置是否正常，是否有网络波动
     * 2.可通过查单接口或等待结果回调通知，获取最终业务处理状态，避免重复交易
     */
    String SDK_INVOKE_IO_EXCEPTION_PREFIX = "sdk.invoke.io";

    /**
     * 错误描述：SDK请求域名熔断
     * 解决方案：
     * 1.检查报错详细堆栈，确定熔断原因
     * 2.默认情况下，YOP-SDK会自动切换域名重试，重试次数为3次
     * 3.可通过查单接口或等待结果回调通知，获取最终业务处理状态，避免重复交易
     */
    String SDK_INVOKE_HOST_BLOCKED_EXCEPTION = "sdk.invoke.host.blocked";

    /**
     * 错误描述：SDK请求未知异常
     * 解决方案：
     * 1.检查报错详细堆栈，确定异常原因
     * 2.可通过查单接口或等待结果回调通知，获取最终业务处理状态，避免重复交易
     */
    String SDK_INVOKE_UNEXPECTED_EXCEPTION = "sdk.invoke.unexpected";


}
