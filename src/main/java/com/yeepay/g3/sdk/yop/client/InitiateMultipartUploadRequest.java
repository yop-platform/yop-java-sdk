package com.yeepay.g3.sdk.yop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: InitiateMultipartUploadRequest<br/>
 * description: 分块上传初始化请求<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/22 下午11:43
 */
public class InitiateMultipartUploadRequest extends MultipartUploadRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateMultipartUploadRequest.class);

    private String bizCode;

    private String fileName;

    public InitiateMultipartUploadRequest() {

    }

    public InitiateMultipartUploadRequest(String appKey) {
        super(appKey);
    }

    public InitiateMultipartUploadRequest(String appKey, String secretKey) {
        super(appKey,secretKey);
    }

    public String getBizCode() {
        return bizCode;
    }

    public InitiateMultipartUploadRequest setBizCode(String bizCode) {
        this.bizCode = bizCode;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public InitiateMultipartUploadRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String toString() {
        return "InitiateMultipartUploadRequest{" +
                "bizCode='" + bizCode + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
