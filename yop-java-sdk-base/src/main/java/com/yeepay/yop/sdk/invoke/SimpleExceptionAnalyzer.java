/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.exception.YopBlockException;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.yeepay.yop.sdk.utils.CharacterConstants.COLON;

/**
 * title: 简单异常分析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/6
 */
public class SimpleExceptionAnalyzer implements ExceptionAnalyzer<AnalyzedException> {

    private final Set<String> excludeExceptions;

    private final Set<String> retryExceptions;

    public SimpleExceptionAnalyzer(Set<String> excludeExceptions, Set<String> retryExceptions) {
        this.excludeExceptions = null != excludeExceptions ? excludeExceptions : Collections.emptySet();
        this.retryExceptions = null != retryExceptions ? retryExceptions : Collections.emptySet();
    }

    @Override
    public AnalyzedException analyze(Throwable e, Object... args) {
        final AnalyzedException result = new AnalyzedException();
        result.setException(e);

        // 客户端异常&业务异常，不重试，不计入熔断笔数
        if (e instanceof YopClientException) {
            result.setExDetail(e.getClass().getCanonicalName() + COLON +
                    StringUtils.defaultString(e.getMessage()));
            return result;
        }

        // 熔断异常，直接重试
        if (e instanceof YopBlockException) {
            result.setExDetail(e.getClass().getCanonicalName() + COLON + StringUtils.defaultString(e.getMessage()));
            result.setNeedRetry(true);
            result.setBlocked(true);
            return result;
        }

        // 分析堆栈，预期异常，可重试
        final Throwable[] allExceptions = ExceptionUtils.getThrowables(e);
        final List<String> exceptionDetails = Lists.newArrayList();
        for (int i = 0; i < allExceptions.length; i++) {
            Throwable rootCause = allExceptions[i];
            final String exType = rootCause.getClass().getCanonicalName(),
                    exTypeAndMsg = exType + COLON + StringUtils.defaultString(rootCause.getMessage());
            exceptionDetails.add(exType);
            exceptionDetails.add(exTypeAndMsg);
            if (retryExceptions.contains(exType) ||
                    retryExceptions.contains(exTypeAndMsg)) {
                result.setExDetail(exTypeAndMsg);
                result.setNeedRetry(true);
                result.setNeedDegrade(true);
                return result;
            }
        }

        // 默认异常消息，取最后一个caused by
        Throwable lastCause = allExceptions[allExceptions.length -1];
        result.setExDetail(lastCause.getClass().getCanonicalName() + COLON +
                StringUtils.defaultString(lastCause.getMessage()));

        // 预期异常，不重试，不计入熔断笔数
        if (CollectionUtils.containsAny(excludeExceptions, exceptionDetails)) {
            return result;
        }

        // 其他异常，不重试，计入熔断笔数
        result.setNeedDegrade(true);
        return result;
    }
}
