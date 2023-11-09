/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

/**
 * title: 异常分析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public interface ExceptionAnalyzer<ExceptionAnalyzeResult> {

    ExceptionAnalyzeResult analyze(Throwable ex, Object... args);
}
