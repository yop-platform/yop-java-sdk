/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.sentinel;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.context.NullContext;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slotchain.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.client.support.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.utils.CharacterConstants;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
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
    private static final YopSph yopSph = new YopSph();

    private static final ThreadPoolExecutor BLOCKED_SWEEPER = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(1000),
            new ThreadFactoryBuilder().setNameFormat("yop-blocked-resource-sweeper-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    static {
        // If init fails, the process will exit.
        InitExecutor.doInit();
    }

    public static YopSph getInstance() {
        return yopSph;
    }

    private static final Object[] OBJECTS0 = new Object[0];

    /**
     * Same resource({@link ResourceWrapper#equals(Object)}) will share the same
     * {@link ProcessorSlotChain}, no matter in which {@link Context}.
     */
    private static volatile Map<ResourceWrapper, ProcessorSlotChain> chainMap = new HashMap<ResourceWrapper, ProcessorSlotChain>();

    private static final Object LOCK = new Object();

    private Entry entryWithPriority(ResourceWrapper resourceWrapper, int count, boolean prioritized, Object... args)
            throws BlockException {
        Context context = ContextUtil.getContext();
        if (context instanceof NullContext) {
            // The {@link NullContext} indicates that the amount of context has exceeded the threshold,
            // so here init the entry only. No rule checking will be done.
            return new YopEntry(resourceWrapper, null, context);
        }

        if (context == null) {
            // Using default context.
            context = InternalContextUtil.internalEnter(Constants.CONTEXT_DEFAULT_NAME);
        }

        // Global switch is close, no rule checking will do.
        if (!Constants.ON) {
            return new YopEntry(resourceWrapper, null, context);
        }

        ProcessorSlot<Object> chain = lookProcessChain(resourceWrapper);

        /*
         * Means amount of resources (slot chain) exceeds {@link Constants.MAX_SLOT_CHAIN_SIZE},
         * so no rule checking will be done.
         */
        if (chain == null) {
            return new YopEntry(resourceWrapper, null, context);
        }

        Entry e = new YopEntry(resourceWrapper, chain, context);
        try {
            chain.entry(context, resourceWrapper, null, count, prioritized, args);
        } catch (BlockException e1) {
            e.exit(count, args);
            throw e1;
        } catch (Throwable e1) {
            // This should not happen, unless there are errors existing in Sentinel internal.
            RecordLog.info("Sentinel unexpected exception", e1);
        }
        return e;
    }

    /**
     * Do all {@link Rule}s checking about the resource.
     *
     * <p>Each distinct resource will use a {@link ProcessorSlot} to do rules checking. Same resource will use
     * same {@link ProcessorSlot} globally. </p>
     *
     * <p>Note that total {@link ProcessorSlot} count must not exceed {@link Constants#MAX_SLOT_CHAIN_SIZE},
     * otherwise no rules checking will do. In this condition, all requests will pass directly, with no checking
     * or exception.</p>
     *
     * @param resourceWrapper resource name
     * @param count           tokens needed
     * @param args            arguments of user method call
     * @return {@link Entry} represents this call
     * @throws BlockException if any rule's threshold is exceeded
     */
    public Entry entry(ResourceWrapper resourceWrapper, int count, Object... args) throws BlockException {
        return entryWithPriority(resourceWrapper, count, false, args);
    }

    /**
     * Get {@link ProcessorSlotChain} of the resource. new {@link ProcessorSlotChain} will
     * be created if the resource doesn't relate one.
     *
     * <p>Same resource({@link ResourceWrapper#equals(Object)}) will share the same
     * {@link ProcessorSlotChain} globally, no matter in which {@link Context}.<p/>
     *
     * <p>
     * Note that total {@link ProcessorSlot} count must not exceed {@link Constants#MAX_SLOT_CHAIN_SIZE},
     * otherwise null will return.
     * </p>
     *
     * @param resourceWrapper target resource
     * @return {@link ProcessorSlotChain} of the resource
     */
    ProcessorSlot<Object> lookProcessChain(ResourceWrapper resourceWrapper) {
        String resourceName = resourceWrapper.getName();
        if (resourceName.contains(UriResource.RESOURCE_SEPERATOR)) {
            final String[] split = resourceName.split(UriResource.RESOURCE_SEPERATOR);
            resourceName = split[split.length -1];
        }
        final StringResourceWrapper realResource = new StringResourceWrapper(resourceName,
                resourceWrapper.getEntryType(), resourceWrapper.getResourceType());
        ProcessorSlotChain chain = chainMap.get(realResource);
        if (chain == null) {
            synchronized (LOCK) {
                chain = chainMap.get(realResource);
                if (chain == null) {
                    // Entry size limit.
                    if (chainMap.size() >= Constants.MAX_SLOT_CHAIN_SIZE) {
                        return null;
                    }

                    chain = SlotChainProvider.newSlotChain();
                    Map<ResourceWrapper, ProcessorSlotChain> newMap = new HashMap<ResourceWrapper, ProcessorSlotChain>(
                            chainMap.size() + 1);
                    newMap.putAll(chainMap);
                    newMap.put(resourceWrapper, chain);
                    chainMap = newMap;
                }
            }
        }
        return chain;
    }

    /**
     * This class is used for skip context name checking.
     */
    private final static class InternalContextUtil extends ContextUtil {
        static Context internalEnter(String name) {
            return trueEnter(name, "");
        }

        static Context internalEnter(String name, String origin) {
            return trueEnter(name, origin);
        }
    }

    public Entry entry(String name) throws BlockException {
        StringResourceWrapper resource = new StringResourceWrapper(name, EntryType.OUT);
        return entry(resource, 1, OBJECTS0);
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
                } catch (Exception e) {
                    LOGGER.warn("blocked sweeper failed, ex:", e);
                }
            });
        }
    }
}
