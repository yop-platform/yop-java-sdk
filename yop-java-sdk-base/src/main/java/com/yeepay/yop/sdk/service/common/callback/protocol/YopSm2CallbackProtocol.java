/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback.protocol;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPlatformCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptProtocol;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptorFactory;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.protocol.AuthenticateProtocolVersion;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequest;
import com.yeepay.yop.sdk.utils.HttpUtils;
import com.yeepay.yop.sdk.utils.X509CertUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.yeepay.yop.sdk.constants.CharacterConstants.*;
import static com.yeepay.yop.sdk.utils.HttpUtils.useEmptyAsCanonicalQueryString;

/**
 * title: Yop-SM2回调<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/16
 */
public class YopSm2CallbackProtocol extends AbstractYopCallbackProtocol {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSm2CallbackProtocol.class);

    private static final Splitter SIGNED_HEADER_STRING_SPLITTER = Splitter.on(SEMICOLON);
    private static final Joiner HEADER_JOINER = Joiner.on(LF);

    /**
     * requestId
     */
    private String yopRequestId;

    /**
     * 证书类型
     */
    private CertTypeEnum certType;

    /**
     * 摘要算法
     */
    private DigestAlgEnum digestAlgEnum;

    /**
     * 协议版本
     */
    private AuthenticateProtocolVersion protocolVersion;

    /**
     * appKey
     */
    private String appKey;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 失效秒数
     */
    private long expirationInSeconds;

    /**
     * 参与签名的header名;分隔
     */
    private String signedHeaders;

    /**
     * 数字签名
     */
    private String signature;

    /**
     * 平台证书序列号
     */
    private String platformSerialNo;

    /**
     * 加密头
     */
    private String yopEncrypt;

    public YopSm2CallbackProtocol(YopCallbackRequest request) {
        initialize(request);
        originRequest = request;
    }

    @Override
    public YopCallback parse() {
        // 验签
        verifySign();

        // 解密
        final String bizContent = decryptBizContent();

        // 返回业务数据
        return YopCallback.builder().withId(yopRequestId).
                withAppKey(appKey).withType(originRequest.getHttpPath())
                .withCreateTime(new Date()).withBizData(bizContent)
                .withMetaInfo("headers", originRequest.getHeaders()).build();
    }

    private void verifySign() {
        String sign = signature;
        String[] args = sign.split("\\$");
        String plainText = preparePlainText();
        final YopPlatformCredentials platformCredentials = YopPlatformCredentialsProviderRegistry.getProvider().
                getCredentials(appKey, platformSerialNo);
        YopSignProcessorFactory.getSignProcessor(certType.getValue()).verify(plainText, args[0], platformCredentials.getCredential());
    }

    private String decryptBizContent() {
        // 解析加密密钥&加密参数
        YopCredentials<?> yopCredentials = YopCredentialsProviderRegistry.getProvider().getCredentials(appKey, certType.getValue());
        final EncryptOptions templateOptions = new EncryptOptions();
        templateOptions.setCredentials(new YopSymmetricCredentials(appKey, ""));
        templateOptions.setCredentialsAlg(certType.getValue());
        YopEncryptProtocol.Inst parsedEncryptProtocol = parseEncryptProtocol(yopEncrypt, yopCredentials, templateOptions);
        if (null == parsedEncryptProtocol) {
            throw new YopClientException("illegal YopSm2CallbackProtocol, request:" + originRequest);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("YopSm2CallbackProtocol to be Decrypted, requestId:{}, headers:{}, params:{}", yopRequestId,
                    parsedEncryptProtocol.getEncryptHeaders(), parsedEncryptProtocol.getEncryptParams());
        }

        // 解密业务数据
        EncryptOptions encryptOptions = parsedEncryptProtocol.getEncryptOptions();
        final YopEncryptor encryptor = YopEncryptorFactory.getEncryptor(encryptOptions.getAlg());
        if (null == originRequest.getContent()) {
            return EMPTY;
        }
        return encryptor.decryptFromBase64((String) originRequest.getContent(), encryptOptions);
    }

    private void initialize(YopCallbackRequest request) {
        try {
            final Map<String, String> headers = request.getCanonicalHeaders();
            String authorization = headers.get(Headers.AUTHORIZATION.toLowerCase());
            String[] protocol = authorization.split(CharacterConstants.SPACE);
            String protocolPrefix = protocol[0], protocolContent = protocol[1];
            String[] parts = protocolPrefix.split(CharacterConstants.DASH_LINE);
            certType = CertTypeEnum.parse(parts[1]);
            digestAlgEnum = DigestAlgEnum.valueOf(parts[2]);
            String[] authorizationHeaders = StringUtils.split(protocolContent, SLASH);
            protocolVersion = AuthenticateProtocolVersion.parse(authorizationHeaders[0]);
            appKey = authorizationHeaders[1];
            timestamp = authorizationHeaders[2];
            expirationInSeconds = Long.parseLong(authorizationHeaders[3]);
            signedHeaders = authorizationHeaders[4].toLowerCase();
            signature = authorizationHeaders[5];
            platformSerialNo = headers.get(Headers.YOP_SIGN_CERT_SERIAL_NO);
            if (StringUtils.isBlank(platformSerialNo)) {
                platformSerialNo = headers.get(Headers.YOP_CERT_SERIAL_NO);
            }
            platformSerialNo = X509CertUtils.parseToHex(platformSerialNo);
            yopEncrypt = headers.get(Headers.YOP_ENCRYPT);
            yopRequestId = headers.get(Headers.YOP_REQUEST_ID);
        } catch (Exception e) {
            throw new YopClientException("error initialize YopSm2CallbackProtocol, ex:", e);
        }
    }

    private String preparePlainText() {
        YopCallbackRequest req = originRequest;
        //authString
        String authString = new StringBuilder(protocolVersion.stringFormat()).append(SLASH)
                .append(appKey).append(SLASH)
                .append(timestamp).append(SLASH)
                .append(expirationInSeconds).toString();

        // Formatting the URL with signing protocol.
        String canonicalURI = HttpUtils.getCanonicalURIPath(req.getHttpPath());

        // Formatting the query string with signing protocol.
        String canonicalQueryString = getCanonicalQueryString();

        // Sorted the headers should be signed from the request.
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = getCanonicalHeaders();
        return new StringBuilder(authString).append(LF)
                .append(req.getHttpPath()).append(LF)
                .append(canonicalURI).append(LF)
                .append(canonicalQueryString).append(LF)
                .append(canonicalHeader).toString();
    }

    private String getCanonicalQueryString() {
        if (useEmptyAsCanonicalQueryString(originRequest.getHttpMethod(), originRequest.getContentType())) {
            return EMPTY;
        }
        return HttpUtils.getCanonicalQueryString(originRequest.getParams(), true);
    }

    private String getCanonicalHeaders() {
        YopCallbackRequest req = originRequest;
        Set<String> headerNames = Sets.newHashSet(SIGNED_HEADER_STRING_SPLITTER.split(signedHeaders));
        List<String> kvs = Lists.newArrayList();
        for (String key : headerNames) {
            final String canonicalKey = key.trim().toLowerCase();
            String value = req.getCanonicalHeaders().get(canonicalKey);
            if (StringUtils.isBlank(value)) {
                continue;
            }
            kvs.add(HttpUtils.normalize(canonicalKey + COLON + HttpUtils.normalize(value.trim())));
        }
        Collections.sort(kvs);
        return HEADER_JOINER.join(kvs);
    }

    private YopEncryptProtocol.Inst parseEncryptProtocol(String encryptProtocol, YopCredentials<?> yopCredentials, EncryptOptions encryptOptions) {
        if (StringUtils.isNotBlank(encryptProtocol)) {
            return YopEncryptProtocol.fromProtocol(encryptProtocol)
                    .parse(new YopEncryptProtocol.ParseParams(encryptProtocol, yopCredentials, encryptOptions));
        }
        return null;
    }
}
