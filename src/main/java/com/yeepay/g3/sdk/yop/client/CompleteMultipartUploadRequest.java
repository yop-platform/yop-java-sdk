package com.yeepay.g3.sdk.yop.client;

import com.yeepay.g3.sdk.yop.model.PartETag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * title: CompleteMultipartUploadRequest<br/>
 * description: 分块上传完成请求<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/23 上午7:15
 */
public class CompleteMultipartUploadRequest extends MultipartUploadRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompleteMultipartUploadRequest.class);

    private String uploadId;

    private String bucket;

    private String key;

    private List<PartETag> parts;

    public CompleteMultipartUploadRequest() {

    }

    public CompleteMultipartUploadRequest(String appKey) {
        super(appKey);
    }

    public CompleteMultipartUploadRequest(String appKey, String secretKey) {
        super(appKey, secretKey);
    }

    public String getUploadId() {
        return uploadId;
    }

    public CompleteMultipartUploadRequest setUploadId(String uploadId) {
        this.uploadId = uploadId;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public CompleteMultipartUploadRequest setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getKey() {
        return key;
    }

    public CompleteMultipartUploadRequest setKey(String key) {
        this.key = key;
        return this;
    }

    public List<PartETag> getParts() {
        return parts;
    }

    public CompleteMultipartUploadRequest setParts(List<PartETag> parts) {
        this.parts = parts;
        return this;
    }

    @Override
    public String toString() {
        return "CompleteMultipartUploadRequest{" +
                "uploadId='" + uploadId + '\'' +
                ", bucket='" + bucket + '\'' +
                ", key='" + key + '\'' +
                ", parts=" + parts +
                '}';
    }
}
