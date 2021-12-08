package com.yeepay.yop.sdk.model.yos;

import com.yeepay.yop.sdk.model.YopResponseMetadata;

/**
 * title: Yos上传response元数据<br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/12/27 11:10
 */
public class YosUploadResponseMetadata extends YopResponseMetadata {

    private String crc64ECMA;

    public String getCrc64ECMA() {
        return crc64ECMA;
    }

    public void setCrc64ECMA(String crc64ECMA) {
        this.crc64ECMA = crc64ECMA;
    }
}
