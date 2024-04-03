/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.router;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.client.ClientExecutionParams;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.Router;
import com.yeepay.yop.sdk.invoke.RouterPolicy;
import com.yeepay.yop.sdk.invoke.model.BlockResource;
import com.yeepay.yop.sdk.invoke.model.Resource;
import com.yeepay.yop.sdk.invoke.model.SimpleRouterParams;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.utils.EnvUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * title: yop域名路由<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/7
 */
public class YopRouter<Input extends BaseRequest, OutPut extends BaseResponse>
        implements Router<UriResource, ClientExecutionParams<Input, OutPut>, ExecutionContext> {

    private final RouterPolicy routerPolicy;

    private final ServerRootSpace serverRootSpace;

    public YopRouter(ServerRootSpace serverRootSpace,
                     RouterPolicy routerPolicy) {
        this.serverRootSpace = serverRootSpace;
        this.routerPolicy = routerPolicy;
    }

    @Override
    public UriResource route(ClientExecutionParams<Input, OutPut> executionParams, ExecutionContext executionContext, Object...args) {
        final String appKey = executionContext.getYopCredentials().getAppKey();
        List<String> invokedServerRoots = null == args[0] ? Collections.emptyList()
                : ((List<?>) args[0]).stream().map(Object::toString).collect(Collectors.toList());
        // 兼容旧版沙箱调用
        if (!EnvUtils.isSandBoxEnv(serverRootSpace.getEnv()) && (EnvUtils.isSandboxApp(appKey) || EnvUtils.isSandBoxMode())) {
            return doSingleRoute(serverRootSpace.getSandboxServerRoot(), ServerRootSpace.ServerRootType.SANDBOX, invokedServerRoots);
        }

        // 手动指定
        Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
        final ServerRootSpace.ServerRootType serverRootType = request.isYosRequest() ? ServerRootSpace.ServerRootType.YOS
                : ServerRootSpace.ServerRootType.COMMON;
        final YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        if (StringUtils.isNotBlank(requestConfig.getServerRoot())) {
            return doSingleRoute(URI.create(requestConfig.getServerRoot()), serverRootType, invokedServerRoots);
        } else {
            URI mainServer = this.serverRootSpace.getMainServers().get(serverRootType);
            if (null == mainServer) {
                throw new YopClientException("Config Error, Main ServerRoot NotFound" + serverRootType);
            }
            final String resourceGroup = UriResource.computeResourceGroup(serverRootSpace.getProvider(),
                    serverRootSpace.getEnv(), serverRootSpace.getServerGroup(), serverRootType);
            final List<URI> backupServers = this.serverRootSpace.getBackupServers().get(serverRootType);
            List<String> availableResources = Lists.newArrayList(new UriResource(resourceGroup, mainServer).computeResourceKey());
            if (CollectionUtils.isNotEmpty(backupServers)) {
                backupServers.forEach(p -> availableResources.add(new UriResource(resourceGroup, p).computeResourceKey()));
            }
            return doBatchRoute(resourceGroup, availableResources, invokedServerRoots);
        }
    }

    private UriResource doBatchRoute(String resourceGroup, List<String> availableResources, List<String> invokedResources) {
        final Resource routeResource = routerPolicy.select(new SimpleRouterParams(resourceGroup,
                availableResources, invokedResources));
        UriResource uriResource = UriResource.parseResourceKey(routeResource.getResourceKey());

        if (routeResource instanceof BlockResource) {
            BlockResource blockResource = (BlockResource) routeResource;
            return new UriResource(UriResource.ResourceType.BLOCKED, uriResource.getResourceGroup(),
                    String.valueOf(blockResource.getBlockSequence()), uriResource.getResource());
        }
        return uriResource;
    }

    private UriResource doSingleRoute(URI serverRoot, ServerRootSpace.ServerRootType serverRootType, List<String> invokeResources) {
        final String resourceGroup = UriResource.computeResourceGroup(serverRootSpace.getProvider(),
                serverRootSpace.getEnv(), serverRootSpace.getServerGroup(), serverRootType);
        UriResource uriResource = new UriResource(resourceGroup, serverRoot);

        final Resource routeResource = routerPolicy.select(new SimpleRouterParams(resourceGroup,
                Collections.singletonList(uriResource.computeResourceKey()), invokeResources));
        if (routeResource instanceof BlockResource) {
            BlockResource blockResource = (BlockResource) routeResource;
            return new UriResource(UriResource.ResourceType.BLOCKED, uriResource.getResourceGroup(),
                    String.valueOf(blockResource.getBlockSequence()), uriResource.getResource());
        }
        return uriResource;
    }

}
