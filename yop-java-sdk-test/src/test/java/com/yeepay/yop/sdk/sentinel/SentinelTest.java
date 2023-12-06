/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    public void testNoRuleEntry() {
        Entry entry = null;
        try {
            entry = SphU.entry("我不存在");
            doBusiness();
        } catch (BlockException e) {
            LOGGER.info("blocked");
        } catch (Throwable ex) {
            LOGGER.error("error, ex:", ex);
        } finally {
            if (null != entry) {
                entry.exit();
            }
        }
        assert null != entry;
    }

    private void doBusiness() {
        LOGGER.info("do business");
    }

    @Test
    @Ignore
    public void testAddRules() throws IOException {
        for (int j = 0; j < 24; j++) {
            final int prefix = j;
            new Thread(() -> {
                for (int i = 0; i < 5000; i++) {
                    final String resourceName = prefix + ":" + i;
                    DegradeRule degradeRule = new DegradeRule(resourceName)
                            .setGrade(2)
                            .setCount(5)
                            .setStatIntervalMs(3000)
                            .setTimeWindow(5);
                    final boolean ruleSetted = DegradeRuleManager.setRulesForResource(resourceName, Sets.newHashSet(degradeRule));
                    assert ruleSetted;
                }
            }).start();
        }
        System.in.read();
    }

}
