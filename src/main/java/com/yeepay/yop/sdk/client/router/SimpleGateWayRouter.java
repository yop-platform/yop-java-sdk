package com.yeepay.yop.sdk.client.router;

import com.google.common.collect.Sets;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfig;
import com.yeepay.g3.core.yop.sdk.sample.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.core.yop.sdk.sample.config.enums.ModeEnum;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.utils.CharacterConstants;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * title: 简单网关路由<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 19:58
 */
public class SimpleGateWayRouter implements GateWayRouter {

    private static final String SYSTEM_SDK_MODE_KEY = "yop.sdk.mode";

    private final ServerRootSpace space;

    private final Set<String> independentApiGroups;

    private final ModeEnum systemMode;

    public SimpleGateWayRouter(ServerRootSpace space) {
        this.space = space;
        this.independentApiGroups = Collections.unmodifiableSet(Sets.newHashSet("bank-encryption"));
        String systemModeConfig = System.getProperty(SYSTEM_SDK_MODE_KEY);
        this.systemMode = StringUtils.isEmpty(systemModeConfig) ? null : ModeEnum.valueOf(systemModeConfig);
    }

    @Override
    public URI route(String appKey, Request request) {
        URI serverRoot;
        if (isAppInSandbox(appKey)) {
            serverRoot = space.getSandboxServerRoot();
        } else {
            serverRoot = request.isYosRequest() ? space.getYosServerRoot() : space.getServerRoot();
            //serviceName是apiGroup的变形，需要还原
            String apiGroup = request.getServiceName().toLowerCase().replace(CharacterConstants.UNDER_LINE, CharacterConstants.DASH_LINE);
            if (independentApiGroups.contains(apiGroup)) {
                try {
                    serverRoot = new URI(serverRoot.getScheme(), serverRoot.getUserInfo(),
                            getIndependentApiGroupHost(apiGroup, serverRoot.getHost(), request.isYosRequest()),
                            serverRoot.getPort(), serverRoot.getPath(), serverRoot.getQuery(), serverRoot.getFragment());
                } catch (Exception ex) {
                    throw new YopClientException("route request failure", ex);
                }
            }
        }
        return serverRoot;
    }

    private boolean isAppInSandbox(String appKey) {
        if (systemMode == null) {
            AppSdkConfig appSdkConfig = AppSdkConfigProviderRegistry.getProvider().getConfig(appKey);
            if (appSdkConfig == null) {
                return false;
            }
            return appSdkConfig.getMode() == ModeEnum.sandbox;
        }
        return systemMode == ModeEnum.sandbox;
    }

    private String getIndependentApiGroupHost(String apiGroup, String originHost, boolean isYosRequest) {
        //目前只有普通api的请求才需要路由到独立网关
        if (isYosRequest) {
            return originHost;
        }
        int index = StringUtils.indexOf(originHost, CharacterConstants.DOT);
        return StringUtils.substring(originHost, 0, index) + CharacterConstants.DASH_LINE + apiGroup + StringUtils.substring(originHost, index);
    }
}
