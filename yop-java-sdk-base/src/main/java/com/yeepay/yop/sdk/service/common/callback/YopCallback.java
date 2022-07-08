/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * title: YOP商户回调(已经过client解密处理)<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/12
 */
public class YopCallback implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * id（标识每一次回调）
     */
    private String id;

    /**
     * 类型(标识业务类型)
     */
    private String type;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 业务数据（json）
     */
    private String bizData;

    /**
     * 发送时间
     */
    private Date createTime;

    /**
     * 其他信息（通知中的非业务参数，或者放在header里的部分字段，会根据需要放在此容器）
     */
    private Map<String, Object> metaInfo = Maps.newHashMap();

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getBizData() {
        return bizData;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Map<String, Object> getMetaInfo() {
        return metaInfo;
    }

    public static Builder builder() {
        final Builder builder = new Builder();
        builder.callback = new YopCallback();
        builder.callback.metaInfo = Maps.newHashMap();
        return builder;
    }

    public static final class Builder {

        private YopCallback callback;

        public YopCallback build() {
            return callback;
        }

        public Builder withId(String id) {
            callback.id = id;
            return this;
        }

        public Builder withType(String type) {
            callback.type = type;
            return this;
        }

        public Builder withAppKey(String appKey) {
            callback.appKey = appKey;
            return this;
        }

        public Builder withBizData(String bizData) {
            callback.bizData = bizData;
            return this;
        }

        public Builder withCreateTime(Date createTime) {
            callback.createTime = createTime;
            return this;
        }

        public Builder withMetaInfo(String key, Object value) {
            callback.metaInfo.put(key, value);
            return this;
        }

        public Builder withMetaInfo(Map<String, Object> metaInfo) {
            if (MapUtils.isNotEmpty(metaInfo)) {
                callback.metaInfo.putAll(metaInfo);
            }
            return this;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
