/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.Env;
import com.alibaba.csp.sentinel.Sph;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.base.cache.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/12/11
 */
public class YopSph {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSph.class);

    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private static final Sph sph = Env.sph;

    private static final ThreadPoolExecutor BLOCKED_SWEEPER = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(1000),
            new ThreadFactoryBuilder().setNameFormat("yop-blocked-resource-sweeper-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static Entry entry(String name) throws BlockException {
        return sph.entry(name);
    }

    public static boolean releaseChainResource(String resourceName) {
        try {
            final Object lock = getCtsphLock();
            assert null != lock;

            final StringResourceWrapper resourceWrapper = new StringResourceWrapper(resourceName, EntryType.OUT);
            synchronized (lock) {
                final Map<ResourceWrapper, ProcessorSlotChain> chainMap = getCtsphChainMap();
                chainMap.remove(resourceWrapper);
            }
            return true;
        } catch (Throwable e) {
            LOGGER.error("error when release sph resource, name:" + resourceName, e);
        }
        return false;
    }

    private static Map<ResourceWrapper, ProcessorSlotChain> getCtsphChainMap() throws NoSuchFieldException, IllegalAccessException {
        return getCtshpField(sph, sph.getClass(), "chainMap");
    }

    private static Object getCtsphLock() throws NoSuchFieldException, IllegalAccessException {
        return getCtshpField(sph, sph.getClass(), "LOCK");
    }

    private static <T> T getCtshpField(Object ctSph, Class<?> aClass,
                                       String fieldName) throws NoSuchFieldException, IllegalAccessException {
        final Field chainMapField = aClass.getDeclaredField(fieldName);
        chainMapField.setAccessible(true);
        return (T) chainMapField.get(ctSph);
    }

    public static class BlockResourcePool {
        private static final Map<String, List<URI>> serverBlockList = Maps.newConcurrentMap();
        private static final Map<String, AtomicLong> serverBLockSequence = Maps.newConcurrentMap();
        public UriResource select(String serverType, URI mainServer) {
            rwl.readLock().lock();
            try {
                URI oldestFailServer = null;
                final List<URI> failedServers = serverBlockList.get(serverType);
                if (null != failedServers && !failedServers.isEmpty()) {
                    oldestFailServer = failedServers.get(0);
                }
                // 熔断列表为空(说明其他线程已半开成功)，选主域名即可
                if (null == oldestFailServer) {
                    oldestFailServer = mainServer;
                }
                return initServer(serverType, oldestFailServer);
            } finally {
                rwl.readLock().unlock();
            }
        }

        private UriResource initServer(String serverType, URI oldestFailServer) {

            final String blockSequenceKey = getBlockSequenceKey(serverType, oldestFailServer);
            final AtomicLong blockSequence = serverBLockSequence.computeIfAbsent(blockSequenceKey,
                    p -> new AtomicLong(0));

            String resourcePrefix = getBlockResourcePrefix(serverType, blockSequence.get());
            return new UriResource(UriResource.ResourceType.BLOCKED,
                    resourcePrefix, oldestFailServer);

        }

        private String parseBlockServerType(String blockResourcePrefix) {
            return blockResourcePrefix.split(CharacterConstants.COMMA)[0];
        }

        private Long parseBLockSequence(String blockResourcePrefix) {
            return Long.valueOf(blockResourcePrefix.split(CharacterConstants.COMMA)[1]);
        }

        private String getBlockResourcePrefix(String serverType, Long blockSequence) {
            return serverType + CharacterConstants.COMMA + blockSequence;
        }

        private String getBlockSequenceKey(String serverType, URI server) {
            return serverType + CharacterConstants.COMMA + server.toString();
        }

        public void onServerStatusChange(UriResource uriResource, CircuitBreaker.State prevState,
                                         CircuitBreaker.State newState, DegradeRule rule,
                                         Set<String> serverRootTypes) {
            updateBlockedStatus(uriResource, serverRootTypes, !CircuitBreaker.State.OPEN.equals(newState));
            if (newState.equals(CircuitBreaker.State.OPEN) && UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType())) {
                asyncDiscardOldServers(uriResource);
            }
        }

        // 更新熔断列表排序
        private void updateBlockedStatus(UriResource uriResource, Set<String> serverRootTypes,
                                         boolean successInvoked) {
            rwl.writeLock().lock();
            try {
                URI serverRoot = uriResource.getResource();
                for (String serverRootType : serverRootTypes) {
                    final List<URI> blockedServers = serverBlockList.computeIfAbsent(serverRootType,
                            p -> new ArrayList<>());
                    blockedServers.removeIf(serverRoot::equals);
                    if (successInvoked) {
                        blockedServers.add(0, serverRoot);
                    } else {
                        blockedServers.add(serverRoot);
                    }
                }
                if (UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType()) && !successInvoked) {
                    final String serverRootType = parseBlockServerType(uriResource.getResourcePrefix());
                    serverBLockSequence.computeIfAbsent(getBlockSequenceKey(serverRootType, uriResource.getResource()),
                            p -> new AtomicLong(0)).getAndAdd(1);
                }

            } finally {
                rwl.writeLock().unlock();
            }
        }

        // 异步清理过期资源
        private void asyncDiscardOldServers(UriResource uriResource) {
            BLOCKED_SWEEPER.submit(() -> {
                try {
                    final String resource = uriResource.computeResourceKey();
                    // 清理资源配置
                    YopDegradeRuleHelper.removeDegradeRule(resource);

                    // 清理资源调用链
                    releaseChainResource(resource);
                } catch (Exception e) {
                    LOGGER.warn("blocked sweeper failed, ex:", e);
                }
            });
        }
    }
}
