package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.exception.config.IllegalConfigFormatException;
import com.yeepay.yop.sdk.exception.config.MissingConfigException;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * title: 校验工具<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 15:48
 */
public class CheckUtils {

    /**
     * 校验sdk配置
     *
     * @param yopFileSdkConfig sdk配置
     */
    public static void checkCustomSDKConfig(YopFileSdkConfig yopFileSdkConfig) {
        if (StringUtils.isEmpty(yopFileSdkConfig.getAppKey())) {
            throw new MissingConfigException("appKey", "appKey is empty");
        }
        if (StringUtils.isNotEmpty(yopFileSdkConfig.getServerRoot())) {
            try {
                new URL(yopFileSdkConfig.getServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormatException("serverRoot", "serverRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(yopFileSdkConfig.getYosServerRoot())) {
            try {
                new URL(yopFileSdkConfig.getYosServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormatException("yosServerRoot", "yosServerRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(yopFileSdkConfig.getSandboxServerRoot())) {
            try {
                new URL(yopFileSdkConfig.getSandboxServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormatException("sandboxServerRoot", "sandboxServerRoot is illegal");
            }
        }
    }

}
