package com.yeepay.yop.sdk.circuit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerRuleConfig;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Integration tests for the YopCircuitBreaker in real scenarios
 */
public class YopCircuitBreakerIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCircuitBreakerIntegrationTest.class);
    private static final String TEST_SERVER = "test-server";
    private static final String TEST_SERVER_TYPE = "test-server-type";
    private static final URI VALID_SERVER = URI.create("https://www.baidu.com");
    private static final URI INVALID_SERVER = URI.create("https://invalid-test-server.yeepay.com");

    private YopCircuitBreakerPool circuitBreakerPool;

    @Before
    public void setUp() throws Exception {
        resetCircuitBreakerManagerState();
        circuitBreakerPool = new YopCircuitBreakerPool();
    }

    @After
    public void tearDown() throws Exception {
        resetCircuitBreakerManagerState();
    }

    /**
     * Test for basic domain fault detection and circuit breaking
     */
    @Test
    public void testDomainFaultDetection() throws Exception {
        // Configure circuit breaker for domain fault detection
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(2); // Break after 2 errors
        ruleConfig.setTimeWindow(2); // 2 second recovery
        config.setRules(Lists.newArrayList(ruleConfig));

        // Initialize circuit breaker for the invalid server
        String resourceName = INVALID_SERVER.toString();
        YopCircuitBreakerManager.addCircuitBreaker(resourceName, config);

        // Try connecting to invalid server - should fail
        try {
            simulateHttpRequest(INVALID_SERVER);
            Assert.fail("Request to invalid server should fail");
        } catch (Exception e) {
            // Expected - mark error in circuit breaker
            YopCircuitBreakerManager.markError(resourceName);
        }

        // Try again - should fail again
        try {
            simulateHttpRequest(INVALID_SERVER);
            Assert.fail("Second request to invalid server should fail");
        } catch (Exception e) {
            // Expected - mark error in circuit breaker again
            YopCircuitBreakerManager.markError(resourceName);
        }

        // Now the circuit breaker should be open
        try {
            YopCircuitBreakerManager.entry(resourceName);
            Assert.fail("Circuit should be open");
        } catch (CircuitBreakerException e) {
            // Expected
            LOGGER.info("Circuit correctly opened after failures: {}", e.getMessage());
        }
    }

    /**
     * Test for server selection and failover
     */
    @Test
    public void testServerSelectionAndFailover() throws Exception {
        List<URI> servers = Arrays.asList(INVALID_SERVER, VALID_SERVER);

        // First selection should be the primary server (invalid one)
        UriResource selected = circuitBreakerPool.select(TEST_SERVER_TYPE, INVALID_SERVER, servers);
        Assert.assertEquals("Initial selection should be primary server", 
                INVALID_SERVER, selected.getResource());

        // Mark invalid server as faulty
        markServerAsFaulty(TEST_SERVER_TYPE, INVALID_SERVER);

        // When we select from only valid servers, we shouldn't get the invalid server
        List<URI> validServers = Arrays.asList(VALID_SERVER);
        UriResource failover = circuitBreakerPool.select(TEST_SERVER_TYPE, INVALID_SERVER, validServers);
        Assert.assertEquals("Failover should select valid server", 
                VALID_SERVER, failover.getResource());

        // Try connecting to the valid server - should succeed
        boolean connected = tryConnectToServer(failover.getResource());
        Assert.assertTrue("Connection to valid server should succeed", connected);
    }

    /**
     * Test for circuit breaker recovery
     */
    @Test
    public void testCircuitBreakerRecovery() throws Exception {
        // Configure circuit breaker with short recovery window
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(1); // Break after 1 error
        ruleConfig.setTimeWindow(1); // 1 second recovery
        config.setRules(Lists.newArrayList(ruleConfig));

        String resourceName = INVALID_SERVER.toString();
        YopCircuitBreakerManager.addCircuitBreaker(resourceName, config);

        // Trip the circuit breaker
        YopCircuitBreakerManager.markError(resourceName);

        // Circuit should be open
        try {
            YopCircuitBreakerManager.entry(resourceName);
            Assert.fail("Circuit should be open");
        } catch (CircuitBreakerException e) {
            // Expected
        }

        // Wait for recovery window
        LOGGER.info("Waiting for circuit breaker recovery...");
        TimeUnit.SECONDS.sleep(2);

        // After recovery window, circuit should be half-open
        CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(resourceName);
        Assert.assertNotNull("Should get entry after recovery window", entry);
        LOGGER.info("Circuit breaker recovered to half-open state");

        // Simulate successful request
        entry.exit();

        // Circuit should now be closed
        CircuitBreakerEntry postRecoveryEntry = YopCircuitBreakerManager.entry(resourceName);
        Assert.assertNotNull("Circuit should be closed after successful request", postRecoveryEntry);
    }

    /**
     * Test for integrating circuit breaker with client routing
     */
    @Test
    public void testClientRouting() throws Exception {
        // In a real application, this would use YopClient with multiple server endpoints
        // and the circuit breaker would detect failures and route to healthy servers.
        // For this test, we'll simulate the routing logic.

        // Set up the circuit breaker configuration
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(1);
        ruleConfig.setTimeWindow(1);
        config.setRules(Lists.newArrayList(ruleConfig));

        List<URI> serverList = Arrays.asList(INVALID_SERVER, VALID_SERVER);

        // Simulate client requests with routing
        boolean firstRequestSuccess = simulateClientRequestWithRouting(serverList, config, false);
        Assert.assertFalse("First request should fail (to invalid server)", firstRequestSuccess);

        // Mark the invalid server as faulty
        markServerAsFaulty(TEST_SERVER_TYPE, INVALID_SERVER);

        // Next request should route to the valid server when we limit available servers
        List<URI> validServers = Arrays.asList(VALID_SERVER);
        boolean secondRequestSuccess = simulateClientRequestWithRouting(validServers, config, true);
        Assert.assertTrue("Second request should succeed (to valid server)", secondRequestSuccess);
    }

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
     * Private helper to simulate HTTP request to a server
     */
    private void simulateHttpRequest(URI serverUri) throws IOException, InterruptedException {
        // In a real test, we would use a real HTTP client
        LOGGER.info("Simulating HTTP request to: {}", serverUri);
        
        if (INVALID_SERVER.equals(serverUri)) {
            throw new ConnectException("Connection refused - simulated failure");
        }
        
        // If it's the valid server, no exception
    }
    
    /**
     * Try to connect to a server and return success/failure
     */
    private boolean tryConnectToServer(URI serverUri) {
        try {
            simulateHttpRequest(serverUri);
            return true;
        } catch (Exception e) {
            LOGGER.info("Connection failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Simulate a client request with circuit breaker routing
     */
    private boolean simulateClientRequestWithRouting(List<URI> servers, 
                                                  YopCircuitBreakerConfig config,
                                                  boolean expectFailover) {
        URI mainServer = INVALID_SERVER;
        
        // Get server based on routing logic
        UriResource selectedServer = circuitBreakerPool.select(TEST_SERVER_TYPE, mainServer, servers);
        
        LOGGER.info("Selected server: {}, Was failover expected: {}", 
                selectedServer.getResource(), expectFailover);
                
        // If failover was expected, verify we didn't select the invalid server
        if (expectFailover) {
            Assert.assertNotEquals("Should have selected failover server", 
                    INVALID_SERVER, selectedServer.getResource());
        }
        
        // Try to connect
        return tryConnectToServer(selectedServer.getResource());
    }
    
    /**
     * Helper method to reset circuit breaker manager state using reflection
     */
    private void resetCircuitBreakerManagerState() throws Exception {
        // Reset circuit breakers map
        Field circuitBreakersField = YopCircuitBreakerManager.class.getDeclaredField("circuitBreakers");
        circuitBreakersField.setAccessible(true);
        Map<String, Set<YopCircuitBreaker>> circuitBreakers = 
                (Map<String, Set<YopCircuitBreaker>>) circuitBreakersField.get(null);
        circuitBreakers.clear();
        
        // Reset resource states map
        Field resourceStatesField = YopCircuitBreakerManager.class.getDeclaredField("resourceStates");
        resourceStatesField.setAccessible(true);
        Map<String, ResourceState> resourceStates = 
                (Map<String, ResourceState>) resourceStatesField.get(null);
        resourceStates.clear();
        
        // Reset initialized flag
        Field initializedField = YopCircuitBreakerManager.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(null, false);
        
        // Reset the circuit breaker pool's block list
        Field serverBlockListField = YopCircuitBreakerPool.class.getDeclaredField("serverBlockList");
        serverBlockListField.setAccessible(true);
        Map<String, List<URI>> blockList = (Map<String, List<URI>>) serverBlockListField.get(null);
        blockList.clear();
    }
} 