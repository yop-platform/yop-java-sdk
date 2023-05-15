package com.yeepay.yop.sdk.utils;

import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopServiceException;
import org.apache.commons.lang3.StringUtils;

/**
 * title:校验工具类 <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/19 18:16
 */
public class ValidateUtils {

    /**
     * 校验客户SDK配置
     *
     * @param customSDKConfig 客户SDK配置
     */
    public static void checkCustomSDKConfig(SDKConfig customSDKConfig) {
        if (StringUtils.isEmpty(customSDKConfig.getAppKey())) {
            throw new YopServiceException("Custom SDKConfig must specify appKey.");
        }
        if (StringUtils.isEmpty(customSDKConfig.getAesSecretKey())
                && (customSDKConfig.getIsvPrivateKey() == null || customSDKConfig.getIsvPrivateKey().length == 0)) {
            throw new YopServiceException("Custom SDKConfig must specify AesSecretKey or IsvPrivateKey.");
        }
    }

}
