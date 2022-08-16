package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.config.IllegalConfigFormatException;
import com.yeepay.yop.sdk.exception.config.MissingConfigException;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

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
    private static final Pattern API_URI_PATTERN = Pattern.compile("/((rest|yos)/.+)?");

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

    /**
     * 校验apiUri
     *
     * @param apiUri 请求接口路径
     */
    public static void checkApiUri(String apiUri) {
        if (StringUtils.isNotBlank(apiUri) && API_URI_PATTERN.matcher(apiUri).matches()) {
            return;
        }
        throw new YopClientException("ReqParam Illegal, ApiUri, value:" + apiUri);
    }

    /**
     * 校验callbackUri
     *
     * @param callbackUri 请求接口路径
     */
    public static void checkCallbackUri(String callbackUri) {
        if (StringUtils.isNotBlank(callbackUri)) {
            return;
        }
        throw new YopClientException("callbackUri is illegal, param:" + callbackUri);
    }

    /**
     * 校验serverRoot
     *
     * @param serverRoot 请求服务器根路径
     */
    public static URI checkServerRoot(String serverRoot) {
        if (null == serverRoot) {
            return null;
        }
        final URI uri;
        try {
            uri = new URI(serverRoot);
        } catch (Exception e) {
            throw new YopClientException("request serverRoot is illegal, value:" + serverRoot);
        }
        if (!StringUtils.equalsAny(uri.getScheme(), "http", "https")) {
            throw new YopClientException("unsupported request scheme, value:" + uri.getScheme());
        }
        return uri;
    }

}
