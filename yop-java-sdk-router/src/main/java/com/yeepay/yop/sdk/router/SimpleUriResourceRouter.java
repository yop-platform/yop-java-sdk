/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.invoke.Router;
import com.yeepay.yop.sdk.invoke.RouterPolicy;
import com.yeepay.yop.sdk.invoke.model.BlockResource;
import com.yeepay.yop.sdk.invoke.model.Resource;
import com.yeepay.yop.sdk.invoke.model.SimpleRouterParams;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * title: 域名路由器，控制每笔请求的域名选择<br>
 * description: 默认实现：优先选用主域名，主域名熔断则选备用域名，主备均发生熔断后，选择最早熔断域名<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/2
 */
public class SimpleUriResourceRouter<Context> implements Router<UriResource, Object, Context> {

    private final String resourceGroup;
    private final List<String> availableResources;
    private final RouterPolicy routerPolicy;

    public SimpleUriResourceRouter(String resourceGroup, List<String> availableUris, RouterPolicy routerPolicy) {
        if (CollectionUtils.isEmpty(availableUris)) {
            throw new YopClientException("availableServerRoots is empty");
        }
        this.resourceGroup = StringUtils.defaultString(resourceGroup, "");
        this.availableResources = new ArrayList<>(availableUris.size());
        for (String uri : availableUris) {
            this.availableResources.add(new UriResource(resourceGroup, URI.create(uri)).computeResourceKey());
        }
        this.routerPolicy = routerPolicy;
    }

    @Override
    public UriResource route(Object inputParams, Context context, Object... args) {
        List<String> invokedResources = null == args[0] ? Collections.emptyList()
                : ((List<?>) args[0]).stream().map(Object::toString).collect(Collectors.toList());

        final Resource routeResource = this.routerPolicy.select(new SimpleRouterParams(this.resourceGroup,
                this.availableResources, invokedResources));
        UriResource uriResource = UriResource.parseResourceKey(routeResource.getResourceKey());

        if (routeResource instanceof BlockResource) {
            BlockResource blockResource = (BlockResource) routeResource;
            return new UriResource(UriResource.ResourceType.BLOCKED, uriResource.getResourceGroup(),
                    String.valueOf(blockResource.getBlockSequence()), uriResource.getResource());
        }
        return uriResource;
    }
}
