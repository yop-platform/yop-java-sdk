package com.yeepay.yop.sdk.auth.req;

import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigestAlgEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * title: 认证需求support<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 16:16
 */
public class AuthorizationReqSupport {

    public static final String SECURITY_RSA2048 = "YOP-RSA2048-SHA256", SECURITY_OAUTH2 = "YOP-OAUTH2";

    private static final Map<String, AuthorizationReq> supportedAuthorizationReqs;

    private static final Map<CertTypeEnum, AuthorizationReq> CERT_SUPPORTED_AUTH_REQS;

    static {
        supportedAuthorizationReqs = new HashMap<String, AuthorizationReq>();
        CERT_SUPPORTED_AUTH_REQS = new HashMap<CertTypeEnum, AuthorizationReq>();
        final AuthorizationReq rsa2048 = AuthorizationReq.Builder.anAuthorizationReq().withSignerType("RSA")
                .withCredentialType("RSA2048")
                .withSignatureAlg("SHA256withRSA")
                .withDigestAlg(DigestAlgEnum.SHA256)
                .withProtocolPrefix("YOP-RSA2048-SHA256")
                .build();
        supportedAuthorizationReqs.put(SECURITY_RSA2048, rsa2048);
        CERT_SUPPORTED_AUTH_REQS.put(CertTypeEnum.RSA2048, rsa2048);

        supportedAuthorizationReqs.put("YOP-RSA2048-SHA512",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType("RSA")
                        .withCredentialType("RSA2048")
                        .withSignatureAlg("SHA512withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA512)
                        .withProtocolPrefix("YOP-RSA2048-SHA512")
                        .build());

        supportedAuthorizationReqs.put("YOP-RSA4096-SHA256",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType("RSA")
                        .withCredentialType("RSA4096")
                        .withSignatureAlg("SHA256withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA256)
                        .withProtocolPrefix("YOP-RSA4096-SHA256")
                        .build());
        supportedAuthorizationReqs.put("YOP-RSA4096-SHA512",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType("RSA")
                        .withCredentialType("RSA4096")
                        .withSignatureAlg("SHA512withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA512)
                        .withProtocolPrefix("YOP-RSA4096-SHA512")
                        .build());
        supportedAuthorizationReqs.put(SECURITY_OAUTH2,
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType("OAUTH2")
                        .withCredentialType("TOKEN")
                        .withDigestAlg(DigestAlgEnum.SHA256)
                        .withProtocolPrefix("Bearer")
                        .build());
    }

    public static AuthorizationReq getAuthorizationReq(String securityReq) {
        return supportedAuthorizationReqs.get(securityReq);
    }

    public static AuthorizationReq getAuthorizationReq(CertTypeEnum certTypeEnum) {
        return CERT_SUPPORTED_AUTH_REQS.get(certTypeEnum);
    }

}
