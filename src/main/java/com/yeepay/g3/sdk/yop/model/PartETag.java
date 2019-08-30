package com.yeepay.g3.sdk.yop.model;

import java.io.Serializable;

/**
 * title: PartETag<br/>
 * description: 分块上传PartETag<br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author genyou.yue
 * @version 1.0
 * @since 2019/8/17 下午8:52
 */
public class PartETag implements Serializable {

    private static final long serialVersionUID = -1L;

    private int partNumber;

    private String eTag;

    public PartETag(int partNumber, String eTag) {
        this.partNumber = partNumber;
        this.eTag = eTag;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public PartETag setPartNumber(int partNumber) {
        this.partNumber = partNumber;
        return this;
    }

    public String geteTag() {
        return eTag;
    }

    public PartETag seteTag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    @Override
    public String toString() {
        return "PartETag{" +
                "partNumber=" + partNumber +
                ", eTag='" + eTag + '\'' +
                '}';
    }
}
