package com.yeepay.yop.sdk.client.support;

import com.yeepay.yop.sdk.Region;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.file.YopHttpClientConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/30 11:35
 */
public class ClientConfigurationSupport {

    public static ClientConfiguration getClientConfiguration(YopSdkConfig yopSdkConfig) {
        ClientConfiguration clientConfiguration = new ClientConfiguration().withEndpoint(yopSdkConfig.getServerRoot());
        if (StringUtils.isNotEmpty(yopSdkConfig.getRegion())) {
            clientConfiguration.withRegion(Region.valueOf(yopSdkConfig.getRegion()));
        }
        if (yopSdkConfig.getProxy() != null) {
            clientConfiguration.withProxyDomain(yopSdkConfig.getProxy().getDomain())
                    .withProxyHost(yopSdkConfig.getProxy().getHost())
                    .withProxyPort(yopSdkConfig.getProxy().getPort())
                    .withProxyScheme(yopSdkConfig.getProxy().getScheme())
                    .withProxyUsername(yopSdkConfig.getProxy().getUsername())
                    .withProxyPassword(yopSdkConfig.getProxy().getPassword())
                    .withProxyWorkstation(yopSdkConfig.getProxy().getWorkstation());
        }
        if (yopSdkConfig.getYopHttpClientConfig() != null) {
            YopHttpClientConfig yopHttpClientConfig = yopSdkConfig.getYopHttpClientConfig();
            clientConfiguration.withMaxConnections(yopHttpClientConfig.getMaxConnTotal())
                    .withConnectionTimeoutInMillis(yopHttpClientConfig.getConnectTimeout())
                    .withConnectionRequestTimeoutInMillis(yopHttpClientConfig.getConnectRequestTimeout())
                    .withSocketTimeoutInMillis(yopHttpClientConfig.getReadTimeout())
                    .withMaxConnectionsPerRoute(yopHttpClientConfig.getMaxConnPerRoute())
                    .withClientImpl(yopHttpClientConfig.getClientImpl());
        }
        return clientConfiguration;
    }
}
