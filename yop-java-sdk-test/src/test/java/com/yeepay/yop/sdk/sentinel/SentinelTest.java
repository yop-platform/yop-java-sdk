/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.sentinel;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotChain;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/12/6
 */
public class SentinelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentinelTest.class);

    @Test
    public void testEntryWithoutResource() {
        int blockCount = 0;
        for (int i = 0; i < 10; i++) {
            final boolean blocked = doEntry("我不存在", true);
            if (blocked) {
                blockCount++;
            }
        }
        assert blockCount == 0;
    }

    private boolean doEntry(String resource, boolean mockFail) {
        Entry entry = null;
        try {
            entry = SphU.entry(resource);
            doBusiness(mockFail);
        } catch (BlockException e) {
            LOGGER.info("blocked");
            return true;
        } catch (Throwable ex) {
            LOGGER.error("error, ex:", ex);
            Tracer.trace(ex);
        } finally {
            if (null != entry) {
                entry.exit();
            }
        }
        assert null != entry;
        return false;
    }

    private void doBusiness(boolean mockFail) {
        if (mockFail) {
            throw new RuntimeException("error");
        }
        LOGGER.info("do business");
    }

    @Test
    @Ignore
    public void testAddRules() throws IOException {
        doAddRules();
        System.in.read();
    }

    private void doAddRules() {
        for (int j = 0; j < 24; j++) {
            final int prefix = j;
            new Thread(() -> {
                for (int i = 0; i < 5000; i++) {
//                    final String resourceName = prefix + ":" + i;
                    // 竞争设置同一资源
                    final String resourceName = "common" + ":" + i;
                    DegradeRule degradeRule = new DegradeRule(resourceName)
                            .setGrade(2)
                            .setCount(5)
                            .setStatIntervalMs(3000)
                            .setTimeWindow(5);
                    Set<DegradeRule> rules = Sets.newHashSet(degradeRule);
                    final boolean ruleSetted = doAddRule(resourceName, rules);
                    if (!ruleSetted) {
                        LOGGER.warn("ruleSetted fail, resource:{}, newRules:{}, oldRules:{}", resourceName, rules,
                                DegradeRuleManager.getRulesOfResource(resourceName));
                    }
//                    assert ruleSetted;
                }
            }).start();
        }
    }

    private synchronized boolean doAddRule(String resourceName, Set<DegradeRule> rules) {
        return DegradeRuleManager.setRulesForResource(resourceName, rules);
    }

    @Test
    public void testEntryButNotBlocked() {
        String resource = "1";
        DegradeRule degradeRule = new DegradeRule(resource)
                .setGrade(2)
                .setCount(1)
                .setStatIntervalMs(3000)
                .setTimeWindow(5);
//        new Thread(() -> {
//            final boolean ruleSetted = DegradeRuleManager.setRulesForResource(resource, Sets.newHashSet(degradeRule));
//            LOGGER.info("son:" + ruleSetted);
//        }).start();
//
//        final boolean ruleSetted = DegradeRuleManager.setRulesForResource(resource, Sets.newHashSet(degradeRule));
//        LOGGER.info("main:" + ruleSetted);
        DegradeRuleManager.loadRules(new ArrayList<>(Sets.newHashSet(degradeRule)));

        for (int i = 0; i < 10; i++) {
            try {
                doEntry(resource, true);
            } catch (Exception e) {
                LOGGER.error("fail:", e);
            }
        }
    }



    @Test
    public void ycTest() throws IOException {
        System.setProperty("yop.sdk.config.env", "qa");
        System.setProperty("yop.sdk.debug", "true");
        YopClient yopClient = YopClientBuilder.builder().build();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                while (true) {
                    try {
                        final YopRequest request = new YopRequest("/rest/v1.0/yop/mock/test", "POST");
                        request.addHeader("mock", "1234");
                        final YopResponse response = yopClient.request(request);
                        LOGGER.error(Thread.currentThread().getName() + "\t-succ-\t" + response.getStringResult());
                    } catch (Exception e) {
                        LOGGER.error(Thread.currentThread().getName() + "\t-fail-\t" + ExceptionUtils.getRootCauseMessage(e));
                    }
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        System.in.read();
    }

    @Test
    public void testReflectSph() throws IllegalAccessException, NoSuchFieldException {
        final CtSph ctSph = (CtSph) Env.sph;
        final Class<? extends CtSph> aClass = ctSph.getClass();

        final Field chainMapField = aClass.getDeclaredField("chainMap");
        chainMapField.setAccessible(true);
        final Map<ResourceWrapper, ProcessorSlotChain> chainMap =
                (Map<ResourceWrapper, ProcessorSlotChain>) chainMapField.get(ctSph);
        assert chainMap.isEmpty();


        final Field lockField = aClass.getDeclaredField("LOCK");
        lockField.setAccessible(true);
        final Object lock = lockField.get(ctSph);
        assert null != lock;
    }
}
