package com.yeepay.yop.sdk.service.common;

import com.yeepay.yop.sdk.model.yos.YosDownloadResponse;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;

/**
 * title: Yop通用client<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 16:13
 */
public interface YopClient {

    /**
     * 普通请求
     *
     * @param request 请求
     * @return 普通返回
     */
    YopResponse request(YopRequest request);

    /**
     * 下载
     *
     * @param request 请求
     * @return yos下载返回
     */
    YosDownloadResponse download(YopRequest request);

    /**
     * 上传
     *
     * @param request 请求
     * @return yos上传返回
     */
    YosUploadResponse upload(YopRequest request);
}
