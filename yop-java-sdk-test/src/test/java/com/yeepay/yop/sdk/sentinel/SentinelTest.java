/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
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

}
