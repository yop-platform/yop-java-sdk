package com.yeepay.yop.sdk.auth.req;

import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.security.DigestAlgEnum;

/**
 * title: 认证需求<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 15:12
 */
public class AuthorizationReq {

    /**
     * 签名类型
     */
    private final String signerType;

    /**
     * 证书类型
     */
    private final String credentialType;

    /**
     * 签名算法
     */
    private final String signatureAlg;

    /**
     * 摘要算法
     */
    private final DigestAlgEnum digestAlg;

    /**
     * 协议前缀
     */
    private final String protocolPrefix;

    private AuthorizationReq(String signerType, String credentialType, String signatureAlg, DigestAlgEnum digestAlg, String protocolPrefix) {
        this.signerType = signerType;
        this.credentialType = credentialType;
        this.signatureAlg = signatureAlg;
        this.digestAlg = digestAlg;
        this.protocolPrefix = protocolPrefix;
    }

    public String getSignerType() {
        return signerType;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public String getSignatureAlg() {
        return signatureAlg;
    }

    public DigestAlgEnum getDigestAlg() {
        return digestAlg;
    }

    public String getProtocolPrefix() {
        return protocolPrefix;
    }

    public SignOptions getSignOptions() {
        return new SignOptions().withDigestAlg(digestAlg).withProtocolPrefix(protocolPrefix);
    }

    public static final class Builder {
        private String signerType;
        private String credentialType;
        private String signatureAlg;
        private DigestAlgEnum digestAlg;
        private String protocolPrefix;

        private Builder() {
        }

        public static Builder anAuthorizationReq() {
            return new Builder();
        }

        public Builder withSignerType(String signerType) {
            this.signerType = signerType;
            return this;
        }

        public Builder withCredentialType(String credentialType) {
            this.credentialType = credentialType;
            return this;
        }

        public Builder withSignatureAlg(String signatureAlg) {
            this.signatureAlg = signatureAlg;
            return this;
        }

        public Builder withDigestAlg(DigestAlgEnum digestAlg) {
            this.digestAlg = digestAlg;
            return this;
        }

        public Builder withProtocolPrefix(String protocolPrefix) {
            this.protocolPrefix = protocolPrefix;
            return this;
        }

        public AuthorizationReq build() {
            return new AuthorizationReq(signerType, credentialType, signatureAlg, digestAlg, protocolPrefix);
        }
    }
}
