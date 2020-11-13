package com.yeepay.yop.sdk.service.common.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.model.yos.BaseYosUploadResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import com.yeepay.yop.sdk.utils.json.KeepAsRawStringDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * title: 通用yos上传返回<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2020-01-03 17:22
 */
public class YosUploadResponse extends BaseYosUploadResponse {

    private static final long serialVersionUID = -1L;

    @JsonIgnore
    private Object result;

    @JsonProperty("result")
    @JsonDeserialize(using = KeepAsRawStringDeserializer.class)
    private String stringResult;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getStringResult() {
        return stringResult;
    }

    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
        if (StringUtils.isNotEmpty(stringResult)) {
            try {
                this.result = JsonUtils.getObjectMapper().readValue(stringResult, Object.class);
            } catch (IOException e) {
                throw new YopClientException("unable to deserialize stringResult:" + stringResult);
            }
        }
    }

}
