package com.yeepay.yop.sdk.client.support;


import com.yeepay.g3.core.yop.sdk.sample.Region;
import com.yeepay.g3.core.yop.sdk.sample.client.ClientConfiguration;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.HttpClientConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/30 11:35
 */
public class ClientConfigurationSupport {

    public static ClientConfiguration getClientConfiguration(AppSdkConfig sdkConfig) {
        ClientConfiguration clientConfiguration = new ClientConfiguration().withEndpoint(sdkConfig.getServerRoot());
        if (StringUtils.isNotEmpty(sdkConfig.getRegion())) {
            clientConfiguration.withRegion(Region.valueOf(sdkConfig.getRegion()));
        }
        if (sdkConfig.getProxy() != null) {
            clientConfiguration.withProxyDomain(sdkConfig.getProxy().getDomain())
                    .withProxyHost(sdkConfig.getProxy().getHost())
                    .withProxyPort(sdkConfig.getProxy().getPort())
                    .withProxyScheme(sdkConfig.getProxy().getScheme())
                    .withProxyUsername(sdkConfig.getProxy().getUsername())
                    .withProxyPassword(sdkConfig.getProxy().getPassword())
                    .withProxyWorkstation(sdkConfig.getProxy().getWorkstation());
        }
        if (sdkConfig.getHttpClientConfig() != null) {
            HttpClientConfig httpClientConfig = sdkConfig.getHttpClientConfig();
            clientConfiguration.withMaxConnections(httpClientConfig.getMaxConnTotal())
                    .withConnectionTimeoutInMillis(httpClientConfig.getConnectTimeout())
                    .withSocketTimeoutInMillis(httpClientConfig.getReadTimeout())
                    .withMaxConnectionsPerRoute(httpClientConfig.getMaxConnPerRoute());
        }
        return clientConfiguration;
    }
}
