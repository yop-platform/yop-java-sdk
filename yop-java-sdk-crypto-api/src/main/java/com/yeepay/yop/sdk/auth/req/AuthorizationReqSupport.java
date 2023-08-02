package com.yeepay.yop.sdk.auth.req;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.SignerTypeEnum;

import java.util.Collections;
import java.util.List;
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

    public static final String SECURITY_SM2 = "YOP-SM2-SM3", SECURITY_RSA2048 = "YOP-RSA2048-SHA256", SECURITY_OAUTH2 = "YOP-OAUTH2";

    private static final Map<String, AuthorizationReq> SUPPORTED_AUTH_REQS;
    private static final Map<CertTypeEnum, AuthorizationReq> CERT_SUPPORTED_AUTH_REQS;
    private static final List<AuthorizationReq> DEFAULT_AUTH_REQS_FOR_API;

    static {
        SUPPORTED_AUTH_REQS = Maps.newHashMap();
        CERT_SUPPORTED_AUTH_REQS = Maps.newHashMap();
        DEFAULT_AUTH_REQS_FOR_API = Lists.newArrayList();

        // api默认
        AuthorizationReq sm2AuthReq = buildAuthorizationReq(SignerTypeEnum.SM2.name(), "SM2",
                "SM3withSM2", DigestAlgEnum.SM3, SECURITY_SM2);
        AuthorizationReq rsaAuthReq = buildAuthorizationReq(SignerTypeEnum.RSA.name(), "RSA2048",
                "SHA256withRSA", DigestAlgEnum.SHA256, SECURITY_RSA2048);

        SUPPORTED_AUTH_REQS.put(SECURITY_SM2, sm2AuthReq);
        SUPPORTED_AUTH_REQS.put(SECURITY_RSA2048, rsaAuthReq);

        CERT_SUPPORTED_AUTH_REQS.put(CertTypeEnum.SM2, sm2AuthReq);
        CERT_SUPPORTED_AUTH_REQS.put(CertTypeEnum.RSA2048, rsaAuthReq);
        DEFAULT_AUTH_REQS_FOR_API.add(sm2AuthReq);
        DEFAULT_AUTH_REQS_FOR_API.add(rsaAuthReq);

        // 其他
        SUPPORTED_AUTH_REQS.put(SECURITY_OAUTH2, buildAuthorizationReq(SignerTypeEnum.OAUTH2.name(), "TOKEN",
                null, DigestAlgEnum.SHA256, "Bearer"));
    }

    public static AuthorizationReq getAuthorizationReq(String securityReq) {
        return SUPPORTED_AUTH_REQS.get(securityReq);
    }

    public static AuthorizationReq getAuthorizationReq(CertTypeEnum certType) {
        return CERT_SUPPORTED_AUTH_REQS.get(certType);
    }

    public static List<AuthorizationReq> getDefaultAuthReqsForApi() {
        return Collections.unmodifiableList(DEFAULT_AUTH_REQS_FOR_API);
    }

    public static AuthorizationReq buildAuthorizationReq(String signerType, String credentialType, String signatureAlg
            , DigestAlgEnum digestAlg, String protocolPrefix) {
        return AuthorizationReq.Builder.anAuthorizationReq().withSignerType(signerType)
                .withCredentialType(credentialType)
                .withSignatureAlg(signatureAlg)
                .withDigestAlg(digestAlg)
                .withProtocolPrefix(protocolPrefix)
                .build();
    }

}
