/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials;

import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.util.List;

/**
 * title: 密钥凭证集合<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/12/1
 */
public class CredentialsCollection implements CredentialsItem {

    public CredentialsCollection(CertTypeEnum certType, List<CredentialsItem> items) {
        this.certType = certType;
        this.items = items;
    }

    /**
     * 密钥类型
     */
    private CertTypeEnum certType;

    /**
     * 密钥凭证集合
     */
    List<CredentialsItem> items;

    @Override
    public CertTypeEnum getCertType() {
        return certType;
    }

    public void setCertType(CertTypeEnum certType) {
        this.certType = certType;
    }

    public List<CredentialsItem> getItems() {
        return items;
    }

    public void setItems(List<CredentialsItem> items) {
        this.items = items;
    }
}
