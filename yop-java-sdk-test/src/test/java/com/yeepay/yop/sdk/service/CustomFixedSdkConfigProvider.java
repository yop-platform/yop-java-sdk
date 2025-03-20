/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service;

import com.yeepay.yop.sdk.base.config.provider.YopFixedSdkConfigProvider;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCertStore;
import com.yeepay.yop.sdk.config.provider.file.YopProxyConfig;

import java.util.Arrays;

/**
 * title: 自定义SDK配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/5/10
 */
public class CustomFixedSdkConfigProvider extends YopFixedSdkConfigProvider {

    @Override
    protected YopSdkConfig loadSdkConfig() {
        YopSdkConfig yopSdkConfig = new YopSdkConfig();

        // 示例：普通接口请求端点地址，配置多个时，遇到故障可以自动切换
        yopSdkConfig.setPreferredServerRoots(Arrays.asList("https://域名A","https://域名B"));
        // 示例：文件上传、下载类接口请求端点地址
        yopSdkConfig.setYosServerRoot("https://域名C");
        // 示例：沙箱环境请求地址
        yopSdkConfig.setSandboxServerRoot("https://域名D");

        // 示例：请求代理配置
        final YopProxyConfig proxyConfig = new YopProxyConfig();
        proxyConfig.setHost("xxx");
        proxyConfig.setPort(1234);
        yopSdkConfig.setProxy(proxyConfig);

        // 示例：国密证书分发配置
        YopCertStore yopCertStore = new YopCertStore();
        yopCertStore.setEnable(true);// 默认会开启本地存储，将远程拉取的证书存放本地文件
        yopCertStore.setPath("/tmp/yop/certs");//默认存放位置
        yopSdkConfig.setYopCertStore(yopCertStore);

        // 连接超时时间、读取超时时间等其他配置，可根据需要setXXX即可
        return yopSdkConfig;
    }

    @Override
    public void removeConfig(String key) {
        // 可以不实现
    }
}
