package com.yeepay.g3.sdk.yop.client;

import java.security.PublicKey;

/**
 * title: 返回结果配置<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-18 15:53
 */
public class ResponseConfig {
    /**
     * 用来标示aes相关签名算法不需要对返回结果验签和解密
     */
    public static final ResponseConfig NONE_OPERATION_CONFIG = new ResponseConfig();
    /**
     * yop公钥用于返回结果验签
     */
    private PublicKey yopPublicKey;

    /**
     * 是否需要解密
     */
    private Boolean needDecrypt;

    /**
     * 解密密钥
     */
    private String decryptKey;

    public PublicKey getYopPublicKey() {
        return yopPublicKey;
    }

    public void setYopPublicKey(PublicKey yopPublicKey) {
        this.yopPublicKey = yopPublicKey;
    }

    public ResponseConfig withYopPublicKey(PublicKey yopPublicKey) {
        this.yopPublicKey = yopPublicKey;
        return this;
    }

    public Boolean isNeedDecrypt() {
        return needDecrypt;
    }

    public void setNeedDecrypt(Boolean needDecrypt) {
        this.needDecrypt = needDecrypt;
    }

    public ResponseConfig withNeedEncrypt(Boolean needEncrypt) {
        this.needDecrypt = needEncrypt;
        return this;
    }

    public String getDecryptKey() {
        return decryptKey;
    }

    public void setDecryptKey(String decryptKey) {
        this.decryptKey = decryptKey;
    }

    public ResponseConfig withEncryptKey(String encryptKey) {
        this.decryptKey = encryptKey;
        return this;
    }
}
