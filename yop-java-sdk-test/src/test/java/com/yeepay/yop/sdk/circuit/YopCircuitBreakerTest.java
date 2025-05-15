package com.yeepay.yop.sdk.circuit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerRuleConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests for the YopCircuitBreaker implementation
 */
public class YopCircuitBreakerTest {

    private static final String TEST_RESOURCE = "test-resource";
    private static final String DOMAIN_RESOURCE = "test-server";
    private static final URI SERVER_URI = URI.create("https://test-server.com");
    
    @Before
    public void setUp() throws Exception {
        // Reset circuit breaker manager state before each test
        resetCircuitBreakerManagerState();
    }
    
    @After
    public void tearDown() throws Exception {
        // Clean up circuit breaker rules after each test
        resetCircuitBreakerManagerState();
    }
    
    /**
     * Test for the error count circuit breaker strategy
     */
    @Test
    public void testErrorCountCircuitBreaker() throws Exception {
        // Create a circuit breaker rule with ERROR_COUNT strategy
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(3); // Trip after 3 errors
        ruleConfig.setTimeWindow(1); // 1 second recovery window for faster testing
        ruleConfig.setMinRequestAmount(1); // Only need 1 request to start evaluating
        config.setRules(Lists.newArrayList(ruleConfig));
        
        // Initialize the circuit breaker with our configuration
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // First request should succeed (circuit is CLOSED)
        CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry", entry);
        
        // Mark 3 errors - this should trip the circuit breaker
        for (int i = 0; i < 3; i++) {
            YopCircuitBreakerManager.markError(TEST_RESOURCE);
        }
        
        // Next request should fail (circuit is OPEN)
        try {
            YopCircuitBreakerManager.entry(TEST_RESOURCE);
            Assert.fail("Circuit should be open and throw exception");
        } catch (CircuitBreakerException e) {
            Assert.assertEquals("Resource name should match", TEST_RESOURCE, e.getResource());
            Assert.assertEquals("Grade should be ERROR_COUNT", 
                    YopCircuitBreakerStrategy.ERROR_COUNT.getType(), e.getRule().getGrade());
        }
        
        // Wait for the recovery window
        Thread.sleep(1100); // 1.1 seconds
        
        // Next request should succeed (circuit is HALF_OPEN)
        CircuitBreakerEntry halfOpenEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry in HALF_OPEN state", halfOpenEntry);
        
        // Complete this request successfully (exit without error)
        halfOpenEntry.exit();
        
        // Next request should succeed (circuit should be CLOSED again)
        CircuitBreakerEntry recoveredEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry after recovery", recoveredEntry);
    }
    
    /**
     * Test for the error ratio circuit breaker strategy
     */
    @Test
    public void testErrorRatioCircuitBreaker() throws Exception {
        // Create a circuit breaker rule with ERROR_RATIO strategy
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_RATIO.getType());
        ruleConfig.setCount(0.5); // Trip at 50% error ratio
        ruleConfig.setTimeWindow(1); // 1 second recovery window
        ruleConfig.setMinRequestAmount(4); // Need 4 requests to start evaluating
        config.setRules(Lists.newArrayList(ruleConfig));
        
        // Initialize the circuit breaker
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // Make 4 requests, 2 with errors (50% error ratio)
        for (int i = 0; i < 4; i++) {
            CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
            if (i % 2 == 0) { // Even requests fail
                entry.exit(new RuntimeException("Test exception"));
            } else {
                entry.exit(); // Odd requests succeed
            }
        }
        
        // Next request should fail (circuit is OPEN)
        try {
            YopCircuitBreakerManager.entry(TEST_RESOURCE);
            Assert.fail("Circuit should be open and throw exception");
        } catch (CircuitBreakerException e) {
            Assert.assertEquals("Resource name should match", TEST_RESOURCE, e.getResource());
            Assert.assertEquals("Grade should be ERROR_RATIO", 
                    YopCircuitBreakerStrategy.ERROR_RATIO.getType(), e.getRule().getGrade());
        }
        
        // Wait for recovery window
        Thread.sleep(1100);
        
        // Next request should succeed (circuit is HALF_OPEN)
        CircuitBreakerEntry halfOpenEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry in HALF_OPEN state", halfOpenEntry);
        halfOpenEntry.exit(); // Exit successfully
        
        // Circuit should now be closed again
        CircuitBreakerEntry recoveredEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry after recovery", recoveredEntry);
    }
    
    /**
     * Test for the slow request ratio circuit breaker strategy
     */
    @Test
    public void testSlowRequestRatioCircuitBreaker() throws Exception {
        // Create a circuit breaker rule with SLOW_REQUEST_RATIO strategy
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        ruleConfig.setCount(50); // RT threshold of 50ms
        ruleConfig.setTimeWindow(1); // 1 second recovery window
        ruleConfig.setMinRequestAmount(3); // Need 3 requests to start evaluating
        config.setRules(Lists.newArrayList(ruleConfig));
        
        // Initialize the circuit breaker
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // Make 3 requests with high response times
        for (int i = 0; i < 3; i++) {
            CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
            // Mark a high response time (100ms)
            YopCircuitBreakerManager.markResponseTime(TEST_RESOURCE, 100);
            entry.exit();
        }
        
        // Next request should fail (circuit is OPEN)
        try {
            YopCircuitBreakerManager.entry(TEST_RESOURCE);
            Assert.fail("Circuit should be open and throw exception");
        } catch (CircuitBreakerException e) {
            Assert.assertEquals("Resource name should match", TEST_RESOURCE, e.getResource());
            Assert.assertEquals("Grade should be SLOW_REQUEST_RATIO", 
                    YopCircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType(), e.getRule().getGrade());
        }
        
        // Wait for recovery window
        Thread.sleep(1100);
        
        // Next request should succeed (circuit is HALF_OPEN)
        CircuitBreakerEntry halfOpenEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry in HALF_OPEN state", halfOpenEntry);
        
        // Mark a fast response time this time
        YopCircuitBreakerManager.markResponseTime(TEST_RESOURCE, 20);
        halfOpenEntry.exit();
        
        // Circuit should now be closed again
        CircuitBreakerEntry recoveredEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Should get a valid entry after recovery", recoveredEntry);
    }
    
    /**
     * Test for state transition back to OPEN during HALF_OPEN on error
     */
    @Test
    public void testHalfOpenToOpenTransition() throws Exception {
        // Create a circuit breaker rule
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(1); // Trip after 1 error
        ruleConfig.setTimeWindow(1); // 1 second recovery window
        ruleConfig.setMinRequestAmount(1);
        config.setRules(Lists.newArrayList(ruleConfig));
        
        // Initialize the circuit breaker
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // Cause an error to trip the circuit
        CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        entry.exit(new RuntimeException("Test exception"));
        
        // Circuit should be OPEN
        try {
            YopCircuitBreakerManager.entry(TEST_RESOURCE);
            Assert.fail("Circuit should be open");
        } catch (CircuitBreakerException e) {
            // Expected
        }
        
        // Wait for recovery window
        Thread.sleep(1100);
        
        // Get entry in HALF_OPEN state
        CircuitBreakerEntry halfOpenEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        
        // Exit with error - should go back to OPEN state
        halfOpenEntry.exit(new RuntimeException("Another test exception"));
        
        // Next request should fail (circuit should be OPEN again)
        try {
            YopCircuitBreakerManager.entry(TEST_RESOURCE);
            Assert.fail("Circuit should be open again after error in HALF_OPEN");
        } catch (CircuitBreakerException e) {
            // Expected
        }
    }
    
    /**
     * Test for multiple circuit breaker rules for a single resource
     */
    @Test
    public void testMultipleRulesForResource() throws Exception {
        // Create a circuit breaker with multiple rules
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        
        // Rule 1: Error count threshold of 5
        YopCircuitBreakerRuleConfig rule1 = new YopCircuitBreakerRuleConfig();
        rule1.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        rule1.setCount(5);
        rule1.setTimeWindow(1);
        rule1.setMinRequestAmount(1);
        
        // Rule 2: Error ratio threshold of 0.7 (70%)
        YopCircuitBreakerRuleConfig rule2 = new YopCircuitBreakerRuleConfig();
        rule2.setGrade(YopCircuitBreakerStrategy.ERROR_RATIO.getType());
        rule2.setCount(0.7);
        rule2.setTimeWindow(1);
        rule2.setMinRequestAmount(3);
        
        config.setRules(Lists.newArrayList(rule1, rule2));
        
        // Initialize the circuit breaker
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // Make 3 requests with 2 errors (67% error ratio, not enough to trigger rule2)
        for (int i = 0; i < 3; i++) {
            CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
            if (i < 2) { 
                entry.exit(new RuntimeException("Test exception"));
            } else {
                entry.exit();
            }
        }
        
        // Circuit should still be CLOSED (error ratio below threshold)
        CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        Assert.assertNotNull("Circuit should still be closed", entry);
        
        // Now trigger 3 more errors (total 5, should trigger rule1)
        for (int i = 0; i < 3; i++) {
            YopCircuitBreakerManager.markError(TEST_RESOURCE);
        }
        
        // Circuit should now be OPEN (error count reached threshold)
        try {
            YopCircuitBreakerManager.entry(TEST_RESOURCE);
            Assert.fail("Circuit should be open after reaching error count threshold");
        } catch (CircuitBreakerException e) {
            Assert.assertEquals("Grade should be ERROR_COUNT", 
                    YopCircuitBreakerStrategy.ERROR_COUNT.getType(), e.getRule().getGrade());
        }
    }
    
    /**
     * Test for state change listener notifications
     */
    @Test
    public void testStateChangeListeners() throws Exception {
        // Create a tracking listener that records state changes
        final AtomicReference<YopCircuitBreaker.State> oldStateRef = new AtomicReference<>(null);
        final AtomicReference<YopCircuitBreaker.State> newStateRef = new AtomicReference<>(null);
        final AtomicInteger callCount = new AtomicInteger(0);
        
        CircuitBreakerStateChangeListener testListener = new CircuitBreakerStateChangeListener() {
            @Override
            public void onStateChange(YopCircuitBreaker.State oldState, YopCircuitBreaker.State newState, YopCircuitBreaker rule) {
                oldStateRef.set(oldState);
                newStateRef.set(newState);
                callCount.incrementAndGet();
            }
        };
        
        // Create a circuit breaker rule
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(1);
        ruleConfig.setTimeWindow(1);
        ruleConfig.setMinRequestAmount(1);
        config.setRules(Lists.newArrayList(ruleConfig));
        
        // Initialize the circuit breaker
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // Register the listener
        YopCircuitBreaker.addStateChangeListener("testListener", testListener);
        
        // Cause an error to trip the circuit (CLOSED -> OPEN)
        CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        entry.exit(new RuntimeException("Test exception"));
        
        // Verify the state change was recorded
        Assert.assertEquals("Old state should be CLOSED", YopCircuitBreaker.State.CLOSED, oldStateRef.get());
        Assert.assertEquals("New state should be OPEN", YopCircuitBreaker.State.OPEN, newStateRef.get());
        Assert.assertEquals("Listener should be called once", 1, callCount.get());
        
        // Reset for next test
        oldStateRef.set(null);
        newStateRef.set(null);
        
        // Wait for recovery window, then get entry (OPEN -> HALF_OPEN)
        Thread.sleep(1100);
        CircuitBreakerEntry halfOpenEntry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
        
        // Verify the state change was recorded
        Assert.assertEquals("Old state should be OPEN", YopCircuitBreaker.State.OPEN, oldStateRef.get());
        Assert.assertEquals("New state should be HALF_OPEN", YopCircuitBreaker.State.HALF_OPEN, newStateRef.get());
        Assert.assertEquals("Listener should be called twice", 2, callCount.get());
        
        // Reset for next test
        oldStateRef.set(null);
        newStateRef.set(null);
        
        // Exit successfully (HALF_OPEN -> CLOSED)
        halfOpenEntry.exit();
        
        // Verify the state change was recorded
        Assert.assertEquals("Old state should be HALF_OPEN", YopCircuitBreaker.State.HALF_OPEN, oldStateRef.get());
        Assert.assertEquals("New state should be CLOSED", YopCircuitBreaker.State.CLOSED, newStateRef.get());
        Assert.assertEquals("Listener should be called three times", 3, callCount.get());
    }
    
    /**
     * Test for circuit breaker pool server selection
     */
    @Test
    public void testCircuitBreakerPoolServerSelection() throws Exception {
        YopCircuitBreakerPool pool = new YopCircuitBreakerPool();
        
        // Create a list of server URIs
        List<URI> servers = Arrays.asList(
                URI.create("https://server1.example.com"),
                URI.create("https://server2.example.com"),
                URI.create("https://server3.example.com")
        );
        
        // First selection should be the main server
        URI mainServer = servers.get(0);
        URI selected = pool.select(DOMAIN_RESOURCE, mainServer, servers).getResource();
        Assert.assertEquals("First selection should be main server", mainServer, selected);
    }
    
    /**
     * Test for concurrent access to circuit breaker manager
     */
    @Test
    public void testConcurrentAccess() throws Exception {
        // Create a circuit breaker with low threshold
        YopCircuitBreakerConfig config = new YopCircuitBreakerConfig();
        YopCircuitBreakerRuleConfig ruleConfig = new YopCircuitBreakerRuleConfig();
        ruleConfig.setGrade(YopCircuitBreakerStrategy.ERROR_COUNT.getType());
        ruleConfig.setCount(10);
        ruleConfig.setTimeWindow(1);
        ruleConfig.setMinRequestAmount(1);
        config.setRules(Lists.newArrayList(ruleConfig));
        
        // Initialize the circuit breaker
        YopCircuitBreakerManager.addCircuitBreaker(TEST_RESOURCE, config);
        
        // Create a thread pool and counters
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        
        // Launch concurrent tasks
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();
                    
                    try {
                        // Get entry
                        CircuitBreakerEntry entry = YopCircuitBreakerManager.entry(TEST_RESOURCE);
                        
                        // Threads with even index cause errors
                        if (threadIndex % 2 == 0) {
                            entry.exit(new RuntimeException("Concurrent test exception"));
                        } else {
                            entry.exit();
                        }
                        
                        successCount.incrementAndGet();
                    } catch (CircuitBreakerException e) {
                        // Circuit is open
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }
        
        // Start all threads
        startLatch.countDown();
        
        // Wait for completion
        doneLatch.await();
        executor.shutdown();
        
        // Verify results - some threads should succeed, some should fail
        // after the circuit breaker trips
        System.out.println("Concurrent test results - Success: " + successCount.get() + ", Failures: " + failureCount.get());
        Assert.assertTrue("Some requests should have succeeded", successCount.get() > 0);
        // Note: We can't guarantee failures if all threads execute very quickly before the circuit trips
    }
    
    /**
     * Helper method to reset circuit breaker manager state using reflection
     */
    private void resetCircuitBreakerManagerState() throws Exception {
        // Access the private fields in YopCircuitBreakerManager
        Field circuitBreakersField = YopCircuitBreakerManager.class.getDeclaredField("circuitBreakers");
        circuitBreakersField.setAccessible(true);
        Map<String, Set<YopCircuitBreaker>> circuitBreakers = 
                (Map<String, Set<YopCircuitBreaker>>) circuitBreakersField.get(null);
        circuitBreakers.clear();
        
        Field resourceStatesField = YopCircuitBreakerManager.class.getDeclaredField("resourceStates");
        resourceStatesField.setAccessible(true);
        Map<String, ResourceState> resourceStates = 
                (Map<String, ResourceState>) resourceStatesField.get(null);
        resourceStates.clear();
        
        Field initializedField = YopCircuitBreakerManager.class.getDeclaredField("initialized");
        initializedField.setAccessible(true);
        initializedField.set(null, false);
        
        // Also clear listeners in YopCircuitBreaker
        Field listenersField = YopCircuitBreaker.class.getDeclaredField("stateChangeListeners");
        listenersField.setAccessible(true);
        Map<String, Map<String, CircuitBreakerStateChangeListener>> listeners =
                (Map<String, Map<String, CircuitBreakerStateChangeListener>>) listenersField.get(null);
        listeners.clear();
    }
} 