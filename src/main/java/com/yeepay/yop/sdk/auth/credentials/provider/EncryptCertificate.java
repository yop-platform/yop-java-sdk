/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.credentials.provider;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/5/21 11:27 AM
 */
public class EncryptCertificate implements Serializable {

    private static final long serialVersionUID = -1L;

    public EncryptCertificate() {
    }

    public EncryptCertificate(String algorithm, String nonce, String associatedData, String ciphertext) {
        this.algorithm = algorithm;
        this.nonce = nonce;
        this.associatedData = associatedData;
        this.ciphertext = ciphertext;
    }

    private String algorithm;

    private String nonce;

    private String associatedData;

    private String ciphertext;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getAssociatedData() {
        return associatedData;
    }

    public void setAssociatedData(String associatedData) {
        this.associatedData = associatedData;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
