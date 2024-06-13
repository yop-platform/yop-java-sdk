/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.router;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.exception.YopBlockException;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;

/**
 * title: 简单异常分析器(支持父类异常配置)<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/2/18
 */
public class SimpleCustomExceptionAnalyzer implements ExceptionAnalyzer<AnalyzedException> {

    private static final Map<String, SimpleCustomExceptionAnalyzer> CACHED_ANALYZERS = Maps.newHashMap();

    private final Set<String> excludeExceptions;

    private final Set<String> retryExceptions;

    public SimpleCustomExceptionAnalyzer(Set<String> excludeExceptions, Set<String> retryExceptions) {
        this.excludeExceptions = null != excludeExceptions ? excludeExceptions : Collections.emptySet();
        this.retryExceptions = null != retryExceptions ? retryExceptions : Collections.emptySet();
    }

    public static SimpleCustomExceptionAnalyzer from(Set<String> excludeExceptions, Set<String> retryExceptions) {
        Set<String> excludes = null != excludeExceptions ? excludeExceptions : Collections.emptySet();
        Set<String> retries = null != retryExceptions ? retryExceptions : Collections.emptySet();
        return CACHED_ANALYZERS.computeIfAbsent(StringUtils.join(excludes, "##") + "," + StringUtils.join(retries, "$$"),
                p -> new SimpleCustomExceptionAnalyzer(excludeExceptions, retryExceptions));
    }

    @Override
    public AnalyzedException analyze(Throwable exception, Object... args) {
        final AnalyzedException result = new AnalyzedException();
        result.setException(exception);

        // 客户端异常&业务异常，不重试，不计入熔断笔数
        if (exception instanceof YopClientException) {
            result.setExDetail(exception.getClass().getCanonicalName() + COLON +
                    StringUtils.defaultString(exception.getMessage()).trim());
            return result;
        }

        // 熔断异常，直接重试
        if (exception instanceof YopBlockException) {
            result.setExDetail(exception.getClass().getCanonicalName() + COLON + StringUtils.defaultString(exception.getMessage()).trim());
            result.setNeedRetry(true);
            result.setBlocked(true);
            return result;
        }

        // 分析堆栈，预期异常，可重试
        final Throwable[] allExceptions = ExceptionUtils.getThrowables(exception);
        final Set<String> exceptionDetails = Sets.newHashSet();
        for (int i = 0; i < allExceptions.length; i++) {
            Throwable rootCause = allExceptions[i];
            // 当前异常
            final String exType = rootCause.getClass().getCanonicalName(),
                    exTypeAndMsg = exType + COLON + StringUtils.defaultString(rootCause.getMessage()).trim();
            exceptionDetails.add(exType);
            exceptionDetails.add(exTypeAndMsg);

            if (retryExceptions.contains(exType) ||
                    retryExceptions.contains(exTypeAndMsg)) {
                result.setExDetail(exTypeAndMsg);
                result.setNeedRetry(true);
                result.setNeedDegrade(true);
                return result;
            }

            // 父类异常
            Set<Class<?>> superClasses = Sets.newHashSet();
            superClasses.addAll(ClassUtils.getAllSuperclasses(rootCause.getClass()));
            superClasses.addAll(ClassUtils.getAllInterfaces(rootCause.getClass()));
            for (Class<?> superClass : superClasses) {
                final String superExType = superClass.getCanonicalName(),
                        superExTypeAndMsg = exType + COLON + StringUtils.defaultString(rootCause.getMessage()).trim();
                exceptionDetails.add(superExType);
                exceptionDetails.add(superExTypeAndMsg);

                if (retryExceptions.contains(superExType) ||
                        retryExceptions.contains(superExTypeAndMsg)) {
                    result.setExDetail(superExTypeAndMsg);
                    result.setNeedRetry(true);
                    result.setNeedDegrade(true);
                    return result;
                }
            }
        }

        // 默认异常消息，取最后一个caused by
        Throwable lastCause = allExceptions[allExceptions.length -1];
        result.setExDetail(lastCause.getClass().getCanonicalName() + COLON +
                StringUtils.defaultString(lastCause.getMessage()).trim());

        // 预期异常，不重试，不计入熔断笔数
        if (CollectionUtils.containsAny(excludeExceptions, exceptionDetails)) {
            return result;
        }

        // 其他异常，不重试，计入熔断笔数
        result.setNeedDegrade(true);
        return result;
    }
}
