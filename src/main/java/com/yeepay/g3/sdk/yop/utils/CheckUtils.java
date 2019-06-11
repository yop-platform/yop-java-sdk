package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.encrypt.Base64;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.exception.config.IllegalConfigFormtException;
import com.yeepay.g3.sdk.yop.exception.config.IllegalConfigLengthException;
import com.yeepay.g3.sdk.yop.exception.config.MissingConfigException;
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
                throw new IllegalConfigFormtException("serverRoot", "serverRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(sdkConfig.getYosServerRoot())) {
            try {
                new URL(sdkConfig.getYosServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormtException("yosServerRoot", "yosServerRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(sdkConfig.getSandboxServerRoot())) {
            try {
                new URL(sdkConfig.getSandboxServerRoot());
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormtException("sandboxServerRoot", "sandboxServerRoot is illegal");
            }
        }
        if (StringUtils.isNotEmpty(sdkConfig.getEncryptKey())) {
            byte[] decoded = Base64.decode(sdkConfig.getEncryptKey().getBytes());
            if (decoded.length != 16 && decoded.length != 32) {
                throw new IllegalConfigLengthException("encryptKey", "encryptKey is illegal");
            }
        }
    }

    /**
     * 校验apiUri
     *
     * @param apiUri apiUri
     */
    public static void checkApiUri(String apiUri) {
        if (StringUtils.isEmpty(apiUri)) {
            throw new YopClientException("apiUri is empty");
        }
        if (!StringUtils.startsWithAny(apiUri, YopConstants.API_URI_PREFIX)) {
            throw new YopClientException("apiUri is illegal");
        }
    }

}
