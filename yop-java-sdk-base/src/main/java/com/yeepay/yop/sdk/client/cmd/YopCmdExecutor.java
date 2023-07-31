/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.cmd;

import java.util.List;

/**
 * title: 命令执行器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/6/6
 */
public interface YopCmdExecutor {
    List<String> support();

    void execute(String cmd, Object... args);
}
