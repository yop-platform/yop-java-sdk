package com.yeepay.yop.sdk.circuit;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Circuit breaker pool to manage server resources
 */
public class YopCircuitBreakerPool {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(YopCircuitBreakerPool.class);
    private static final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    
    private static final ThreadPoolExecutor BLOCKED_SWEEPER = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(1000),
            new ThreadFactoryBuilder().setNameFormat("yop-blocked-resource-sweeper-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(BLOCKED_SWEEPER::shutdownNow));
    }
    
    // Map of server type to blocked server list
    private static final Map<String, List<URI>> serverBlockList = Maps.newConcurrentMap();
    
    // Map of server block sequence
    private static final Map<String, AtomicLong> serverBlockSequence = Maps.newConcurrentMap();
    
    /**
     * Select a server from the pool
     * 
     * @param serverType the server type
     * @param mainServer the primary server
     * @param allServers all available servers
     * @return UriResource for the selected server
     */
    public UriResource select(String serverType, URI mainServer, List<URI> allServers) {
        rwl.readLock().lock();
        try {
            URI oldestFailServer = null;
            final List<URI> failedServers = serverBlockList.get(serverType);
            if (null != failedServers && !failedServers.isEmpty()) {
                for (URI failedServer : failedServers) {
                    if (CollectionUtils.isNotEmpty(allServers) && allServers.contains(failedServer)) {
                        oldestFailServer = failedServer;
                        break;
                    }
                }
            }
            // If no failed servers in the list (other threads already successfully half-opened), select the main server
            if (null == oldestFailServer) {
                oldestFailServer = mainServer;
            }
            return initServer(serverType, oldestFailServer);
        } finally {
            rwl.readLock().unlock();
        }
    }
    
    /**
     * Initialize a server resource
     */
    private UriResource initServer(String serverType, URI server) {
        final String blockSequenceKey = getBlockSequenceKey(serverType, server);
        final AtomicLong blockSequence = serverBlockSequence.computeIfAbsent(blockSequenceKey,
                p -> new AtomicLong(0));
        
        String resourcePrefix = getBlockResourcePrefix(serverType, blockSequence.get());
        return new UriResource(UriResource.ResourceType.BLOCKED, resourcePrefix, server);
    }
    
    /**
     * Parse server type from a block resource prefix
     */
    private String parseBlockServerType(String blockResourcePrefix) {
        return blockResourcePrefix.split(CharacterConstants.COMMA)[0];
    }
    
    /**
     * Parse block sequence from a block resource prefix
     */
    private Long parseBlockSequence(String blockResourcePrefix) {
        return Long.valueOf(blockResourcePrefix.split(CharacterConstants.COMMA)[1]);
    }
    
    /**
     * Generate a block resource prefix
     */
    private String getBlockResourcePrefix(String serverType, Long blockSequence) {
        return serverType + CharacterConstants.COMMA + blockSequence;
    }
    
    /**
     * Generate a block sequence key
     */
    private String getBlockSequenceKey(String serverType, URI server) {
        return serverType + CharacterConstants.COMMA + server.toString();
    }
    
    /**
     * Handle server status changes
     */
    public void onServerStatusChange(UriResource uriResource, YopCircuitBreaker.State prevState,
                                  YopCircuitBreaker.State newState, YopCircuitBreaker rule,
                                  Set<String> serverRootTypes) {
        updateBlockedStatus(uriResource, serverRootTypes, !YopCircuitBreaker.State.OPEN.equals(newState));
        if (newState.equals(YopCircuitBreaker.State.OPEN) && 
            UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType())) {
            asyncDiscardOldServers(uriResource);
        }
    }
    
    /**
     * Update blocked status for a server
     */
    private void updateBlockedStatus(UriResource uriResource, Set<String> serverRootTypes, boolean successInvoked) {
        rwl.writeLock().lock();
        try {
            URI serverRoot = uriResource.getResource();
            for (String serverRootType : serverRootTypes) {
                final List<URI> blockedServers = serverBlockList.computeIfAbsent(serverRootType,
                        p -> new ArrayList<>());
                blockedServers.removeIf(serverRoot::equals);
                if (successInvoked) {
                    blockedServers.add(0, serverRoot); // Add to front for successful invocation
                } else {
                    blockedServers.add(serverRoot); // Add to end for failed invocation
                }
            }
            
            if (UriResource.ResourceType.BLOCKED.equals(uriResource.getResourceType()) && !successInvoked) {
                final String serverRootType = parseBlockServerType(uriResource.getResourcePrefix());
                serverBlockSequence.computeIfAbsent(getBlockSequenceKey(serverRootType, uriResource.getResource()),
                        p -> new AtomicLong(0)).getAndAdd(1);
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }
    
    /**
     * Asynchronously discard old server resources
     */
    private void asyncDiscardOldServers(UriResource uriResource) {
        BLOCKED_SWEEPER.submit(() -> {
            try {
                final String resource = uriResource.computeResourceKey();
                // Clean up resource configuration
                YopCircuitBreakerManager.removeCircuitBreaker(resource);
            } catch (Exception e) {
                LOGGER.warn("Blocked sweeper failed, ex:", e);
            }
        });
    }
} 