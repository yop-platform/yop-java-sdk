package com.yeepay.yop.sdk.model.yos;

import com.yeepay.yop.sdk.model.YopResponseMetadata;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * title: yos下载返回元数据<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/28 15:16
 */
public class YosDownloadResponseMetadata extends YopResponseMetadata {

    private static final long serialVersionUID = -1L;

    private long instanceLength = -1;

    /**
     * The cacheControl is used for controlling HTTP caching.
     */
    private String cacheControl;

    /**
     * The offset of the append object.
     */
    private long appendOffset;

    YosDownloadResponseMetadata() {
    }

    public long getInstanceLength() {
        return instanceLength;
    }

    public void setInstanceLength(long instanceLength) {
        this.instanceLength = instanceLength;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public void setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public long getAppendOffset() {
        return appendOffset;
    }

    public void setAppendOffset(long appendOffset) {
        this.appendOffset = appendOffset;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
