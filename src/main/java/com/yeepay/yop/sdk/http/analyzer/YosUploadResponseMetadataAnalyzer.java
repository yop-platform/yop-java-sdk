package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.g3.core.yop.sdk.sample.http.Headers;
import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseAnalyzer;
import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseHandleContext;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseResponse;
import com.yeepay.g3.core.yop.sdk.sample.model.yos.BaseYosUploadResponse;
import com.yeepay.g3.core.yop.sdk.sample.model.yos.YosUploadResponseMetadata;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/12/26 20:57
 */
public class YosUploadResponseMetadataAnalyzer implements HttpResponseAnalyzer {

    private static final YosUploadResponseMetadataAnalyzer INSTANCE = new YosUploadResponseMetadataAnalyzer();

    public static YosUploadResponseMetadataAnalyzer getInstance() {
        return INSTANCE;
    }

    public YosUploadResponseMetadataAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        if (response instanceof BaseYosUploadResponse) {
            YosUploadResponseMetadata uploadResponseMetadata = ((BaseYosUploadResponse)response).getMetadata();
            uploadResponseMetadata.setCrc64ECMA(context.getResponse().getHeader(Headers.YOP_HASH_CRC64ECMA));
        }
        return false;
    }
}
