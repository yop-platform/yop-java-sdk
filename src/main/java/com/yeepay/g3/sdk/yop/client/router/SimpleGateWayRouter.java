package com.yeepay.g3.sdk.yop.client.router;

import com.google.common.collect.Sets;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.config.AppSdkConfig;
import com.yeepay.g3.sdk.yop.config.AppSdkConfigProviderRegistry;
import com.yeepay.g3.sdk.yop.config.enums.ModeEnum;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.utils.CharacterConstants;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
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
 * @since 2019-03-13 10:40
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
    public String route(String apiUri, YopRequest request) {
        String serverRoot;
        if (isAppInSandbox(request.getAppSdkConfig().getAppKey())) {
            serverRoot = space.getSandboxServerRoot();
        } else {
            String apiGroup = extractApiGroupFromApiUri(apiUri);
            if (independentApiGroups.contains(apiGroup)) {
                boolean isYosRequest = isYosRequest(apiUri, request);
                URL serverRootURL = isYosRequest ? space.getYosServerRootURL() : space.getServerRootURL();
                URL independentServerRootURL;
                try {
                    independentServerRootURL = new URL(serverRootURL.getProtocol(), getIndependentApiGroupHost(apiGroup, serverRootURL.getHost(), isYosRequest),
                            serverRootURL.getPort(), serverRootURL.getFile());
                } catch (MalformedURLException e) {
                    throw new YopClientException("route request failure");
                }
                serverRoot = independentServerRootURL.toString();
            } else {
                serverRoot = isYosRequest(apiUri, request) ? space.getYosServerRoot() : space.getServerRoot();
            }
        }
        return serverRoot;
    }

    private boolean isYosRequest(String apiUri, YopRequest request) {
        boolean isYosRequest = false;
        if (MapUtils.isNotEmpty(request.getMultipartFiles())) {
            isYosRequest = true;
            return isYosRequest;
        }
        if (StringUtils.startsWith(apiUri, "/yos")) {
            isYosRequest = true;
            return isYosRequest;
        }
        return isYosRequest;
    }

    private String extractApiGroupFromApiUri(String apiUri) {
        int startIndex = StringUtils.ordinalIndexOf(apiUri, CharacterConstants.SLASH, 3);
        if (startIndex < -1 || startIndex == apiUri.length() - 1) {
            throw new YopClientException("illegal apiUri:" + apiUri);
        }
        int endIndex = StringUtils.indexOf(apiUri, CharacterConstants.SLASH, startIndex + 1);
        if (endIndex < -1) {
            throw new YopClientException("illegal apiUri:" + apiUri);
        }
        return StringUtils.mid(apiUri, startIndex + 1, endIndex - startIndex - 1);
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
        if (isYosRequest) {
            return originHost;
        }
        int index = StringUtils.indexOf(originHost, CharacterConstants.DOT);
        return StringUtils.substring(originHost, 0, index) + CharacterConstants.DASH_LINE + apiGroup + StringUtils.substring(originHost, index);
    }
}
