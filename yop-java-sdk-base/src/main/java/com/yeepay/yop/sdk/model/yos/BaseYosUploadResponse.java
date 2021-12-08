package com.yeepay.yop.sdk.model.yos;

import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;

/**
 * title: Yos上传返回基类<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/12/27 11:17
 */
public abstract class BaseYosUploadResponse extends BaseResponse {

    private static final long serialVersionUID = -1L;

    @Override
    protected YopResponseMetadata getMetaDataInstance() {
        return new YosUploadResponseMetadata();
    }

    @Override
    public YosUploadResponseMetadata getMetadata() {
        return (YosUploadResponseMetadata) super.getMetadata();
    }
}
