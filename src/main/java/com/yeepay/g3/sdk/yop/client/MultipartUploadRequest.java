package com.yeepay.g3.sdk.yop.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * title: MultipartUploadRequest<br/>
 * description: 分块上传基类<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/22 下午11:47
 */
@JsonIgnoreProperties({"apiUri", "appKey", "secretKey", "needEncrypt", "encryptKey"})
public class MultipartUploadRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartUploadRequest.class);

    private String apiUri;
    /**
     * 应用标识
     */
    private String appKey;
    /**
     * 可支持不同请求使用不同的appKey及secretKey,secretKey只用于本地签名，不会被提交
     */
    private String secretKey;
    /**
     * 请求是否需要加密
     */
    private Boolean needEncrypt;
    /**
     * 加密密钥
     */
    private String encryptKey;

    public MultipartUploadRequest() {

    }

    public MultipartUploadRequest(String appKey) {
        Validate.notBlank(appKey, "AppKey is blank.");
        this.appKey = appKey;
    }

    public MultipartUploadRequest(String appKey, String secretKey) {
        Validate.notBlank(appKey, "AppKey is blank.");
        Validate.notBlank(secretKey, "SecretKey is blank.");
        this.appKey = appKey;
        this.secretKey = secretKey;
    }

    public String getApiUri() {
        return apiUri;
    }

    public MultipartUploadRequest setApiUri(String apiUri) {
        this.apiUri = apiUri;
        return this;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public Boolean getNeedEncrypt() {
        return needEncrypt;
    }

    public MultipartUploadRequest setNeedEncrypt(Boolean needEncrypt) {
        this.needEncrypt = needEncrypt;
        return this;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public MultipartUploadRequest setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
        return this;
    }

    @Override
    public String toString() {
        return "MultipartUploadRequest{" +
                "apiUri='" + apiUri + '\'' +
                ", appKey='" + appKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", needEncrypt=" + needEncrypt +
                ", encryptKey='" + encryptKey + '\'' +
                '}';
    }
}
