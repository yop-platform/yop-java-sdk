/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router.sentinel;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.invoke.model.BlockResource;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * title: sentinel资源监控数据封装<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/3/29
 */
public class YopSentinelMetricsHelper {

    private static final ReentrantReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    /**
     * 历史熔断资源列表，按自定义规则分组，组内资源对等，按熔断时间倒序排列，可互相切换
     */
    private static final Map<String, LinkedList<String>> BLOCK_RESOURCE_BY_GROUP = Maps.newConcurrentMap();

    /**
     * 资源熔断次数序列号
     */
    private static final Map<String, AtomicLong> RESOURCE_BLOCK_COUNTER = Maps.newConcurrentMap();

    /**
     * 资源监控数据读写锁
     *
     * @return ReentrantReadWriteLock
     */
    public static ReentrantReadWriteLock getMetricsReadWriteLock() {
        return READ_WRITE_LOCK;
    }


    /**
     * 资源熔断事件
     *
     * @param resourceGroup     资源分组
     * @param resource          资源
     * @param isBlockedResource 是否为熔断资源
     */
    public static void onResourceBlocked(String resourceGroup, String resource, boolean isBlockedResource) {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            LinkedList<String> blockResources = BLOCK_RESOURCE_BY_GROUP.get(resourceGroup);
            if (null == blockResources) {
                blockResources = new LinkedList<>();
            } else {
                blockResources.remove(resource);
            }
            blockResources.add(resource);

            // 熔断资源，再次熔断时，计数器自增
            if (isBlockedResource) {
                AtomicLong resourceSequence = RESOURCE_BLOCK_COUNTER.get(resource);
                if (null == resourceSequence) {
                    resourceSequence = new AtomicLong(1);
                    RESOURCE_BLOCK_COUNTER.put(resource, resourceSequence);
                } else {
                    resourceSequence.getAndAdd(1);
                }
            }

        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 资源熔断恢复事件
     *
     * @param resourceGroup 资源分组
     * @param resource      资源
     */
    public static void onResourceAvailable(String resourceGroup, String resource) {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            LinkedList<String> blockResources = BLOCK_RESOURCE_BY_GROUP.get(resourceGroup);
            if (null == blockResources) {
                blockResources = new LinkedList<>();
            } else {
                blockResources.remove(resource);
            }
            blockResources.addFirst(resource);
        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

    /**
     * 获取分组内最早熔断资源
     *
     * @param resourceGroup 资源分组
     * @return BlockResource
     */
    public static BlockResource findFirstBlockResourceByGroup(String resourceGroup) {
        READ_WRITE_LOCK.readLock().lock();
        try {
            LinkedList<String> blockResources = BLOCK_RESOURCE_BY_GROUP.get(resourceGroup);
            if (null == blockResources || blockResources.isEmpty()) {
                return null;
            }

            final String firstBlockResource = blockResources.peek();
            if (null == firstBlockResource) {
                return null;
            }

            AtomicLong resourceSequence = RESOURCE_BLOCK_COUNTER.get(firstBlockResource);
            if (null == resourceSequence) {
                READ_WRITE_LOCK.readLock().unlock();
                READ_WRITE_LOCK.writeLock().lock();
                try {
                    resourceSequence = RESOURCE_BLOCK_COUNTER.get(firstBlockResource);
                    if (null == resourceSequence) {
                        resourceSequence = new AtomicLong(1);
                        RESOURCE_BLOCK_COUNTER.put(firstBlockResource, resourceSequence);
                    }
                    READ_WRITE_LOCK.readLock().lock();
                } finally {
                    READ_WRITE_LOCK.writeLock().unlock();
                }
            }
            return new BlockResource(firstBlockResource, resourceSequence.get());
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }

    /**
     * 获取当前熔断资源
     *
     * @param resource 资源
     * @return BlockResource
     */
    public static BlockResource findCurrentBlockResource(String resource) {
        READ_WRITE_LOCK.readLock().lock();
        try {
            AtomicLong resourceSequence = RESOURCE_BLOCK_COUNTER.get(resource);
            if (null == resourceSequence) {
                READ_WRITE_LOCK.readLock().unlock();
                READ_WRITE_LOCK.writeLock().lock();
                try {
                    resourceSequence = RESOURCE_BLOCK_COUNTER.get(resource);
                    if (null == resourceSequence) {
                        resourceSequence = new AtomicLong(1);
                        RESOURCE_BLOCK_COUNTER.put(resource, resourceSequence);
                    }
                    READ_WRITE_LOCK.readLock().lock();
                } finally {
                    READ_WRITE_LOCK.writeLock().unlock();
                }
            }
            return new BlockResource(resource, resourceSequence.get());
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }
}
