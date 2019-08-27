package com.yeepay.g3.sdk.yop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: UploadPartRequest<br/>
 * description: 分块上传请求<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/23 上午12:49
 */
public class UploadPartRequest extends MultipartUploadRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadPartRequest.class);

    private String uploadId;

    private int partNumber;

    private int partSize;

    private String bucket;

    private String key;

    private Object file;

    public UploadPartRequest() {

    }

    public UploadPartRequest(String appKey) {
        super(appKey);
    }

    public UploadPartRequest(String appKey, String secretKey) {
        super(appKey,secretKey);
    }

    public String getUploadId() {
        return uploadId;
    }

    public UploadPartRequest setUploadId(String uploadId) {
        this.uploadId = uploadId;
        return this;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public UploadPartRequest setPartNumber(int partNumber) {
        this.partNumber = partNumber;
        return this;
    }

    public int getPartSize() {
        return partSize;
    }

    public UploadPartRequest setPartSize(int partSize) {
        this.partSize = partSize;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public UploadPartRequest setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getKey() {
        return key;
    }

    public UploadPartRequest setKey(String key) {
        this.key = key;
        return this;
    }

    public Object getFile() {
        return file;
    }

    public UploadPartRequest setFile(Object file) {
        this.file = file;
        return this;
    }

    @Override
    public String toString() {
        return "UploadPartRequest{" +
                "uploadId='" + uploadId + '\'' +
                ", partNumber=" + partNumber +
                ", partSize=" + partSize +
                ", bucket='" + bucket + '\'' +
                ", key='" + key + '\'' +
                ", file=" + file +
                '}';
    }
}
