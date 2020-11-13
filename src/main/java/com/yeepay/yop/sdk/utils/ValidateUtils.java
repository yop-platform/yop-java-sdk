package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.config.provider.file.YopFileSdkConfig;
import com.yeepay.yop.sdk.exception.YopServiceException;
import org.apache.commons.lang3.StringUtils;

/**
 * title:校验工具类 <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/19 18:16
 */
public class ValidateUtils {

    /**
     * 校验客户SDK配置
     *
     * @param customYopFileSdkConfig 客户SDK配置
     */
    public static void checkCustomSDKConfig(YopFileSdkConfig customYopFileSdkConfig) {
        if (StringUtils.isEmpty(customYopFileSdkConfig.getAppKey())) {
            throw new YopServiceException("Custom SDKConfig must specify appKey.");
        }
        if (StringUtils.isEmpty(customYopFileSdkConfig.getAesSecretKey())
                && (customYopFileSdkConfig.getIsvPrivateKey() == null || customYopFileSdkConfig.getIsvPrivateKey().length == 0)) {
            throw new YopServiceException("Custom SDKConfig must specify AesSecretKey or IsvPrivateKey.");
        }
    }

}
