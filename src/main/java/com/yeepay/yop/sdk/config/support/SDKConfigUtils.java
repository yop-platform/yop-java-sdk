package com.yeepay.yop.sdk.config.support;

import com.yeepay.g3.core.yop.sdk.sample.config.SDKConfig;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopServiceException;
import com.yeepay.g3.core.yop.sdk.sample.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * title: sdk配置支持类<br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/26 下午3:50
 */
public final class SDKConfigUtils {

    public static SDKConfig loadConfig(String configFile) {
        InputStream fis = null;
        SDKConfig config;
        try {
            fis = ConfigUtils.getInputStream(configFile);
            config = JsonUtils.loadFrom(fis, SDKConfig.class);
        } catch (Exception ex) {
            throw new YopServiceException("Errors occurred when loading SDK Config.", ex);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (StringUtils.endsWith(config.getServerRoot(), "/")) {
            config.setServerRoot(StringUtils.substring(config.getServerRoot(), 0, -1));
        }
        return config;
    }

    public static SDKConfig loadConfig(InputStream in) {
        InputStream fis = null;
        SDKConfig config;
        try {
            config = JsonUtils.loadFrom(in, SDKConfig.class);
        } catch (Exception ex) {
            throw new YopServiceException("Errors occurred when loading SDK Config.", ex);
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (StringUtils.endsWith(config.getServerRoot(), "/")) {
            config.setServerRoot(StringUtils.substring(config.getServerRoot(), 0, -1));
        }
        return config;
    }

}

