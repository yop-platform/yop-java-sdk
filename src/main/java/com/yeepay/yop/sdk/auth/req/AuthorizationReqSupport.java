package com.yeepay.yop.sdk.auth.req;

import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.SignerTypeEnum;

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

    private static final Map<String, AuthorizationReq> supportedAuthorizationReqs;

    static {
        supportedAuthorizationReqs = new HashMap<String, AuthorizationReq>();
        supportedAuthorizationReqs.put("YOP-SM2-SM3",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType(SignerTypeEnum.SM2)
                        .withCredentialType("SM2")
                        .withSignatureAlg("SM3withSM2")
                        .withDigestAlg(DigestAlgEnum.SM3)
                        .withProtocolPrefix("YOP-SM2-SM3")
                        .build());
        supportedAuthorizationReqs.put("YOP-RSA2048-SHA256",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType(SignerTypeEnum.RSA)
                        .withCredentialType("RSA2048")
                        .withSignatureAlg("SHA256withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA256)
                        .withProtocolPrefix("YOP-RSA2048-SHA256")
                        .build());
        supportedAuthorizationReqs.put("YOP-RSA2048-SHA512",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType(SignerTypeEnum.RSA)
                        .withCredentialType("RSA2048")
                        .withSignatureAlg("SHA512withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA512)
                        .withProtocolPrefix("YOP-RSA2048-SHA512")
                        .build());

        supportedAuthorizationReqs.put("YOP-RSA4096-SHA256",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType(SignerTypeEnum.RSA)
                        .withCredentialType("RSA4096")
                        .withSignatureAlg("SHA256withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA256)
                        .withProtocolPrefix("YOP-RSA4096-SHA256")
                        .build());
        supportedAuthorizationReqs.put("YOP-RSA4096-SHA512",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType(SignerTypeEnum.RSA)
                        .withCredentialType("RSA4096")
                        .withSignatureAlg("SHA512withRSA")
                        .withDigestAlg(DigestAlgEnum.SHA512)
                        .withProtocolPrefix("YOP-RSA4096-SHA512")
                        .build());
        supportedAuthorizationReqs.put("YOP-OAUTH2",
                AuthorizationReq.Builder.anAuthorizationReq().withSignerType(SignerTypeEnum.OAUTH2)
                        .withCredentialType("TOKEN")
                        .withDigestAlg(DigestAlgEnum.SHA256)
                        .withProtocolPrefix("Bearer")
                        .build());
    }

    public static AuthorizationReq getAuthorizationReq(String securityReq) {
        return supportedAuthorizationReqs.get(securityReq);
    }

}
