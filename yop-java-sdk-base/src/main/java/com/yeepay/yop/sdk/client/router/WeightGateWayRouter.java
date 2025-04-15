package com.yeepay.yop.sdk.client.router;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * title: 带权重的网关路由<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 19:58
 */
public class WeightGateWayRouter extends AbstractGateWayRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeightGateWayRouter.class);

    public WeightGateWayRouter(ServerRootSpace space) {
        super(space);
    }

    @Override
    protected ServerRootRouting initServerRootRouting(ServerRootSpace space) {
        if (null == space.getUriWeight() || space.getUriWeight().isEmpty()) {
            return super.initServerRootRouting(space);
        }


        final Map<ServerRootType, URI> mainServers = Maps.newConcurrentMap();
        final Map<ServerRootType, List<URI>> backupServers = Maps.newConcurrentMap();

        // 按权重选主:common
        final List<RouteUtils.WeightAble<URI>> weightCommonList = RouteUtils.weightList(space.getPreferredEndPoint()
                .stream().map(p -> new RouteUtils.WeightAble<>(p, space.getUriWeight().getOrDefault(p, 0)))
                .collect(Collectors.toList()));
        if (recordMainServer(weightCommonList.remove(0).getT(), ServerRootType.COMMON, mainServers)) {
            backupServers.put(ServerRootType.COMMON, weightCommonList.stream()
                    .map(RouteUtils.WeightAble::getT).collect(Collectors.toList()));
        }
        // yos
        final List<RouteUtils.WeightAble<URI>> weightYosList = RouteUtils.weightList((CollectionUtils.isEmpty(space.getPreferredYosEndPoint())
                ? Lists.newArrayList(space.getYosServerRoot()) : space.getPreferredYosEndPoint())
                .stream().map(p -> new RouteUtils.WeightAble<>(p, space.getUriWeight().getOrDefault(p, 0)))
                .collect(Collectors.toList()));
        if (recordMainServer(weightYosList.remove(0).getT(), ServerRootType.YOS, mainServers)) {
            backupServers.put(ServerRootType.YOS, weightYosList.stream()
                    .map(RouteUtils.WeightAble::getT).collect(Collectors.toList()));
        }
        // sandbox 兼容老沙箱
        final List<URI> randomSandboxList = RouteUtils.randomList(Lists.newArrayList(space.getSandboxServerRoot()));
        if (recordMainServer(randomSandboxList.remove(0), ServerRootType.SANDBOX, mainServers)) {
            backupServers.put(ServerRootType.SANDBOX, randomSandboxList);
        }
        return new ServerRootRouting(mainServers, backupServers);
    }
}
