/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.sentinel;

import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Constants;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Entry;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.EntryType;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.context.Context;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.context.ContextUtil;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.context.NullContext;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.init.InitExecutor;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.log.RecordLog;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slotchain.*;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * title: yop定制sentinel代码，重写部分代码<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/12/11
 */
public class YopSph {

    private static final YopSph yopSph = new YopSph();

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
            context = YopSph.InternalContextUtil.internalEnter(Constants.CONTEXT_DEFAULT_NAME);
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
}
