package com.yeepay.g3.sdk.yop.utils;

import com.google.common.base.Charsets;
import com.yeepay.g3.sdk.yop.client.YopConstants;
import com.yeepay.g3.sdk.yop.config.SDKConfig;
import com.yeepay.g3.sdk.yop.encrypt.Base64;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.exception.config.IllegalConfigFormtException;
import com.yeepay.g3.sdk.yop.exception.config.IllegalConfigLengthException;
import com.yeepay.g3.sdk.yop.exception.config.MissingConfigException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

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
        checkServerRoot(sdkConfig.getServerRoot(), "serverRoot");
        checkServerRoot(sdkConfig.getYosServerRoot(), "yosServerRoot");
        checkServerRoot(sdkConfig.getSandboxServerRoot(), "sandboxServerRoot");
        if (CollectionUtils.isNotEmpty(sdkConfig.getPreferredServerRoots())) {
            for (String preferredServerRoot : sdkConfig.getPreferredServerRoots()) {
                checkServerRoot(preferredServerRoot, "preferredServerRoot");
            }
        }
        if (CollectionUtils.isNotEmpty(sdkConfig.getPreferredYosServerRoots())) {
            for (String yosPreferredServerRoot : sdkConfig.getPreferredYosServerRoots()) {
                checkServerRoot(yosPreferredServerRoot, "yosPreferredServerRoot");
            }
        }
        if (StringUtils.isNotEmpty(sdkConfig.getEncryptKey())) {
            byte[] decoded = Base64.decode(sdkConfig.getEncryptKey().getBytes(Charsets.UTF_8));
            if (decoded.length != 16 && decoded.length != 32) {
                throw new IllegalConfigLengthException("encryptKey", "encryptKey is illegal");
            }
        }
    }

    private static void checkServerRoot(String serverRoot, String serverRootType) {
        if (StringUtils.isNotEmpty(serverRoot)) {
            try {
                new URL(serverRoot);
            } catch (MalformedURLException e) {
                throw new IllegalConfigFormtException(serverRootType, serverRootType + " is illegal");
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

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message + " must be specified");
        }
    }

    public static void notEmpty(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message + " must be specified");
        } else if (obj instanceof String && obj.toString().trim().length() == 0) {
            throw new IllegalArgumentException(message + " must be specified");
        } else if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            throw new IllegalArgumentException(message + " must be specified");
        } else if (obj instanceof Collection && ((Collection)obj).isEmpty()) {
            throw new IllegalArgumentException(message + " must be specified");
        } else if (obj instanceof Map && ((Map)obj).isEmpty()) {
            throw new IllegalArgumentException(message + " must be specified");
        }
    }

}
