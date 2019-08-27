package com.yeepay.g3.sdk.yop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: AbortMultipartUploadRequest<br/>
 * description: 分块上传取消请求<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/23 上午12:35
 */
public class AbortMultipartUploadRequest extends MultipartUploadRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbortMultipartUploadRequest.class);

    private String uploadId;

    private String bucket;

    private String key;

    public AbortMultipartUploadRequest() {

    }

    public AbortMultipartUploadRequest(String appKey) {
        super(appKey);
    }

    public AbortMultipartUploadRequest(String appKey, String secretKey) {
        super(appKey,secretKey);
    }

    public String getUploadId() {
        return uploadId;
    }

    public AbortMultipartUploadRequest setUploadId(String uploadId) {
        this.uploadId = uploadId;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public AbortMultipartUploadRequest setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getKey() {
        return key;
    }

    public AbortMultipartUploadRequest setKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public String toString() {
        return "AbortMultipartUploadRequest{" +
                "uploadId='" + uploadId + '\'' +
                ", bucket='" + bucket + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
