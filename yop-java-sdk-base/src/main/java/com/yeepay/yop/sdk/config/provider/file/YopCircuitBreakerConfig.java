/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.config.provider.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerRuleConfig.DEFAULT_ERROR_COUNT_CONFIG;

/**
 * title: 熔断配置<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/28
 */
public class YopCircuitBreakerConfig implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final YopCircuitBreakerConfig DEFAULT_CONFIG = new YopCircuitBreakerConfig();

    /**
     * 启用断路器
     */
    @JsonProperty("enable")
    private boolean enable = true;

    /**
     * 熔断规则
     */
    @JsonProperty("rules")
    private List<YopCircuitBreakerRuleConfig> rules = Lists.newArrayList(DEFAULT_ERROR_COUNT_CONFIG);

    // region yop扩展
    /**
     * 非短路异常
     */
    @JsonProperty("yop_exclude_exceptions")
    private Set<String> excludeExceptions = Sets.newHashSet("java.net.SocketTimeoutException:Read timed out");
    // endRegion

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<YopCircuitBreakerRuleConfig> getRules() {
        return rules;
    }

    public void setRules(List<YopCircuitBreakerRuleConfig> rules) {
        if (null != rules && rules.size() > 0) {
            this.rules = rules;
        }
    }

    public Set<String> getExcludeExceptions() {
        return excludeExceptions;
    }

    public void setExcludeExceptions(Set<String> excludeExceptions) {
        if (null != excludeExceptions && excludeExceptions.size() > 0) {
            this.excludeExceptions = excludeExceptions;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
