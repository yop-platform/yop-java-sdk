package com.yeepay.g3.sdk.yop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: UploadFileRequest<br/>
 * description: 文件上传接口<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/23 上午7:32
 */
public class UploadFileRequest extends MultipartUploadRequest{

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadPartRequest.class);

    private String bizCode;

    private Object file;
    /**
     * 文件上传标志 1-小文件上传（小于5M） 2-非小文件上传(大于5M或不知文件大小)
     */
    private String uploadFlag;

    private String fileName;

    public UploadFileRequest() {

    }

    public UploadFileRequest(String appKey) {
        super(appKey);
    }

    public UploadFileRequest(String appKey, String secretKey) {
        super(appKey,secretKey);
    }

    public String getBizCode() {
        return bizCode;
    }

    public UploadFileRequest setBizCode(String bizCode) {
        this.bizCode = bizCode;
        return this;
    }

    public Object getFile() {
        return file;
    }

    public UploadFileRequest setFile(Object file) {
        this.file = file;
        return this;
    }

    public String getUploadFlag() {
        return uploadFlag;
    }

    public UploadFileRequest setUploadFlag(String uploadFlag) {
        this.uploadFlag = uploadFlag;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public UploadFileRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public String toString() {
        return "UploadFileRequest{" +
                "bizCode='" + bizCode + '\'' +
                ", file=" + file +
                ", uploadFlag='" + uploadFlag + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
