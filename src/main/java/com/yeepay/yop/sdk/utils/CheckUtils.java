package com.yeepay.yop.sdk.utils;

import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;
import com.yeepay.g3.core.yop.sdk.sample.exception.config.IllegalConfigFormatException;
import com.yeepay.g3.core.yop.sdk.sample.exception.config.MissingConfigException;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * title: 校验工具<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/8/17 15:48
 */
public class CheckUtils {

    /**
     * 校验sdk配置
     *
     * @param sdkConfig sdk配置
     */
    public static void checkCustomSDKConfig(SDKConfig sdkConfig) {
        if (StringUtils.isEmpty(sdkConfig.getAppKey())) {
            throw new MissingConfigException("appKey", "appKey is empty");
        }
        if (StringUtils.isNotEmpty(sdkConfig.getServerRoot())) {
            try {
                new URL(sdkConfig.getServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormatException("serverRoot", "serverRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(sdkConfig.getYosServerRoot())) {
            try {
                new URL(sdkConfig.getYosServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormatException("yosServerRoot", "yosServerRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(sdkConfig.getSandboxServerRoot())) {
            try {
                new URL(sdkConfig.getSandboxServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormatException("sandboxServerRoot", "sandboxServerRoot is illegal");
            }
        }
    }

}
