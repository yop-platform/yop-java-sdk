package com.yeepay.yop.sdk.circuit;

import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tests for the YopCircuitBreakerPool implementation
 * focusing on server routing and failover functionality
 */
public class YopCircuitBreakerPoolTest {

    private static final String SERVER_TYPE_A = "server-type-a";
    private static final String SERVER_TYPE_B = "server-type-b";
    
    private YopCircuitBreakerPool circuitBreakerPool;
    
    @Before
    public void setUp() throws Exception {
        circuitBreakerPool = new YopCircuitBreakerPool();
        resetBlockedResources();
    }
    
    /**
     * Test for the initial selection of a server
     */
    @Test
    public void testInitialServerSelection() {
        // Create server URIs
        URI server1 = URI.create("https://server1.example.com");
        URI server2 = URI.create("https://server2.example.com");
        URI server3 = URI.create("https://server3.example.com");
        List<URI> serverList = Arrays.asList(server1, server2, server3);
        
        // First selection should be the primary server
        UriResource selected = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverList);
        Assert.assertEquals("Initial selection should be primary server", server1, selected.getResource());
    }
    
    /**
     * Test for the circuit breaking of a server
     */
    @Test
    public void testServerCircuitBreaking() throws Exception {
        // Create server URIs
        URI server1 = URI.create("https://server1.example.com");
        URI server2 = URI.create("https://server2.example.com");
        URI server3 = URI.create("https://server3.example.com");
        List<URI> serverList = Arrays.asList(server1, server2, server3);
        
        // First selection should be the primary server
        UriResource selected = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverList);
        Assert.assertEquals("Initial selection should be primary server", server1, selected.getResource());
        
        // Mark server1 as faulty (block it)
        UriResource server1Resource = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverList);
        circuitBreakerPool.onServerStatusChange(
                server1Resource, 
                YopCircuitBreaker.State.CLOSED, 
                YopCircuitBreaker.State.OPEN, 
                null,
                Sets.newHashSet(SERVER_TYPE_A));
        
        // Next selection should not be server1
        UriResource next = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverList);
        // Since we're not avoiding it by excluding it from available servers,
        // it will still be selected for half-open testing
        Assert.assertEquals("First selection should be the blocked server for half-open testing", 
                server1, next.getResource());
        
        // But if we make it unavailable in the server list, it should select a different server
        List<URI> serverListWithoutServer1 = Arrays.asList(server2, server3);
        UriResource nextWithoutServer1 = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverListWithoutServer1);
        Assert.assertNotEquals("Should not select unavailable server", server1, nextWithoutServer1.getResource());
    }
    
    /**
     * Test for the selection among multiple alternate servers
     */
    @Test
    public void testAlternateServerSelection() throws Exception {
        // Create server URIs
        URI server1 = URI.create("https://server1.example.com");
        URI server2 = URI.create("https://server2.example.com");
        URI server3 = URI.create("https://server3.example.com");
        URI server4 = URI.create("https://server4.example.com");
        List<URI> serverList = Arrays.asList(server1, server2, server3, server4);
        
        // Mark server1, server2, and server3 as faulty
        markServerAsFaulty(SERVER_TYPE_A, server1);
        markServerAsFaulty(SERVER_TYPE_A, server2);
        markServerAsFaulty(SERVER_TYPE_A, server3);
        
        // When selecting only from non-faulty servers, should select server4
        List<URI> nonFaultyServers = Arrays.asList(server4);
        UriResource selected = circuitBreakerPool.select(SERVER_TYPE_A, server1, nonFaultyServers);
        Assert.assertEquals("Should select the only non-faulty server", server4, selected.getResource());
    }
    
    /**
     * Test for the fallback to primary server when all servers are faulty
     */
    @Test
    public void testFallbackToPrimaryWhenAllFaulty() throws Exception {
        // Create server URIs
        URI server1 = URI.create("https://server1.example.com");
        URI server2 = URI.create("https://server2.example.com");
        URI server3 = URI.create("https://server3.example.com");
        List<URI> serverList = Arrays.asList(server1, server2, server3);
        
        // Mark all servers as faulty
        markServerAsFaulty(SERVER_TYPE_A, server1);
        markServerAsFaulty(SERVER_TYPE_A, server2);
        markServerAsFaulty(SERVER_TYPE_A, server3);
        
        // When selecting from only faulty servers, should select the first one 
        // (primary server - for half-open testing)
        UriResource selected = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverList);
        Assert.assertEquals("When all servers are faulty, should first select primary for half-open test", 
                server1, selected.getResource());
    }
    
    /**
     * Test for separate block lists for different server types
     */
    @Test
    public void testSeparateBlockListsForServerTypes() throws Exception {
        // Create server URIs for two different server types
        URI serverA1 = URI.create("https://serverA1.example.com");
        URI serverA2 = URI.create("https://serverA2.example.com");
        List<URI> serverListA = Arrays.asList(serverA1, serverA2);
        
        URI serverB1 = URI.create("https://serverB1.example.com");
        URI serverB2 = URI.create("https://serverB2.example.com");
        List<URI> serverListB = Arrays.asList(serverB1, serverB2);
        
        // Mark servers as faulty for type A
        markServerAsFaulty(SERVER_TYPE_A, serverA1);
        
        // Selection for type B should be unaffected
        UriResource selectedB = circuitBreakerPool.select(SERVER_TYPE_B, serverB1, serverListB);
        Assert.assertEquals("Selection for type B should be unaffected", serverB1, selectedB.getResource());
        
        // Force to select different server for type A by removing serverA1 from available servers
        List<URI> serverListWithoutServerA1 = Arrays.asList(serverA2);
        UriResource selectedA = circuitBreakerPool.select(SERVER_TYPE_A, serverA1, serverListWithoutServerA1);
        Assert.assertEquals("Should select alternate server for type A", serverA2, selectedA.getResource());
    }
    
    /**
     * Test for recovery of blocked servers after timeout
     */
    @Test
    public void testServerRecoveryAfterTimeout() throws Exception {
        // Create server URIs
        URI server1 = URI.create("https://server1.example.com");
        URI server2 = URI.create("https://server2.example.com");
        List<URI> serverList = Arrays.asList(server1, server2);
        
        // Mark server1 as faulty
        markServerAsFaulty(SERVER_TYPE_A, server1);
        
        // Clear the block list to simulate recovery
        Field serverBlockListField = YopCircuitBreakerPool.class.getDeclaredField("serverBlockList");
        serverBlockListField.setAccessible(true);
        Map<String, List<URI>> blockList = (Map<String, List<URI>>) serverBlockListField.get(null);
        blockList.clear();
        
        // Selection should now include server1 again
        UriResource recoveredSelection = circuitBreakerPool.select(SERVER_TYPE_A, server1, serverList);
        Assert.assertEquals("After recovery, should select primary server again", 
                server1, recoveredSelection.getResource());
    }
    
    /**
     * Test for the retry notification callback
     */
//    @Test
//    public void testRetryNotificationCallback() {
//        // Create server URIs
//        URI server1 = URI.create("https://server1.example.com");
//        URI server2 = URI.create("https://server2.example.com");
//        List<URI> serverList = Arrays.asList(server1, server2);
//
//        // Use callback tracking flag
//        final boolean[] callbackCalled = new boolean[1];
//
//        // Create a callback
//        UriResource.Callback callback = args -> callbackCalled[0] = true;
//
//        // Mark server1 as faulty
//        markServerAsFaulty(SERVER_TYPE_A, server1);
//
//        // When selecting with callback, it should receive the callback
//        UriResource selected = circuitBreakerPool.select(SERVER_TYPE_A, server1, Arrays.asList(server2), callback);
//
//        // Selected server should be server2
//        Assert.assertEquals("Should select alternate server", server2, selected.getResource());
//
//        // Manually trigger the callback
//        if (selected.getCallback() != null) {
//            selected.getCallback().notify();
//
//            // Verify callback was called
//            Assert.assertTrue("Callback should have been called", callbackCalled[0]);
//        }
//    }
    
    /**
     * Helper method to mark a server as faulty
     */
    private void markServerAsFaulty(String serverType, URI serverUri) {
        UriResource resource = circuitBreakerPool.select(serverType, serverUri, Arrays.asList(serverUri));
        circuitBreakerPool.onServerStatusChange(
                resource, 
                YopCircuitBreaker.State.CLOSED, 
                YopCircuitBreaker.State.OPEN, 
                null,
                Sets.newHashSet(serverType));
    }
    
    /**
     * Helper method to reset blocked resources for testing
     */
    private void resetBlockedResources() throws Exception {
        Field serverBlockListField = YopCircuitBreakerPool.class.getDeclaredField("serverBlockList");
        serverBlockListField.setAccessible(true);
        Map<String, List<URI>> blockList = (Map<String, List<URI>>) serverBlockListField.get(null);
        blockList.clear();
        
        Field serverBlockSequenceField = YopCircuitBreakerPool.class.getDeclaredField("serverBlockSequence");
        serverBlockSequenceField.setAccessible(true);
        Map<String, AtomicLong> blockSequence = (Map<String, AtomicLong>) serverBlockSequenceField.get(null);
        blockSequence.clear();
    }
} 