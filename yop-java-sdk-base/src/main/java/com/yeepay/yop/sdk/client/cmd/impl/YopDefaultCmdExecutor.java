/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.cmd.impl;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.client.cmd.YopCmdExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.yeepay.yop.sdk.constants.CharacterConstants.ASTERISK;

/**
 * title: 默认命令执行器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/6/6
 */
public class YopDefaultCmdExecutor implements YopCmdExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopDefaultCmdExecutor.class);
    private static final List<String> SUPPORT_TYPES = Lists.newArrayList(ASTERISK);
    @Override
    public List<String> support() {
        return SUPPORT_TYPES;
    }

    @Override
    public void execute(String cmd, Object... args) {
        LOGGER.info("Received YopCmd:{}", cmd);
    }
}
