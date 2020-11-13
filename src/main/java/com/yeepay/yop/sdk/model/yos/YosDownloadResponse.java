package com.yeepay.yop.sdk.model.yos;

import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * title: Yos下载response<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/28 15:46
 */
public class YosDownloadResponse extends BaseResponse {

    private static final long serialVersionUID = -5217869650925279439L;

    private YosDownloadInputStream result;

    @Override
    protected YopResponseMetadata getMetaDataInstance() {
        return new YosDownloadResponseMetadata();
    }

    @Override
    public YosDownloadResponseMetadata getMetadata() {
        return (YosDownloadResponseMetadata) super.getMetadata();
    }

    public YosDownloadInputStream getResult() {
        return result;
    }

    public void setResult(YosDownloadInputStream result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
