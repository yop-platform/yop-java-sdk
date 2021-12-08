package com.yeepay.yop.sdk.model;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: BaseResponse<br>
 * description: Represents the response from an YOP service, including the result payload and any response metadata. YOP response
 * metadata consists primarily of the YOP request ID, which can be used for debugging purposes when services aren't
 * acting as expected.<br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/10/27 11:05
 */
public abstract class BaseResponse implements Serializable, Cloneable {

    private static final long serialVersionUID = -1L;

    protected YopResponseMetadata metadata;

    public BaseResponse() {
        this.metadata = getMetaDataInstance();
    }

    protected YopResponseMetadata getMetaDataInstance() {
        return new YopResponseMetadata();
    }

    public YopResponseMetadata getMetadata() {
        return this.metadata;
    }

    protected String[] excludeFieldNames() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    @Override
    public String toString() {
        String[] excludeFieldNames = excludeFieldNames();
        if (ArrayUtils.isNotEmpty(excludeFieldNames)) {
            return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .setExcludeFieldNames(excludeFieldNames)
                    .toString();
        }
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
