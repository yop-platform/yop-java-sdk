/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.signer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CertificateCredentials;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopCredentialsWithoutSign;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.base.security.digest.YopDigesterFactory;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.internal.RestartableInputStream;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.SignerTypeEnum;
import com.yeepay.yop.sdk.utils.DateUtils;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yeepay.yop.sdk.YopConstants.DEFAULT_YOP_PROTOCOL_VERSION;

/**
 * title: YopPKISigner<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:25 下午
 */
public class YopPKISigner implements YopSigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopPKISigner.class);
    private static final Set<String> DEFAULT_HEADERS_TO_SIGN = Sets.newHashSet();
    private static final Joiner HEADER_JOINER = Joiner.on('\n');
    private static final Joiner SIGNED_HEADER_STRING_JOINER = Joiner.on(';');

    static {
        DEFAULT_HEADERS_TO_SIGN.add(Headers.CONTENT_LENGTH.toLowerCase());
        DEFAULT_HEADERS_TO_SIGN.add(Headers.CONTENT_TYPE.toLowerCase());
        DEFAULT_HEADERS_TO_SIGN.add(Headers.CONTENT_MD5.toLowerCase());
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_REQUEST_ID);
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_DATE);
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_APPKEY);
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_CONTENT_SHA256);
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_HASH_CRC64ECMA);
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_CONTENT_SM3);
        DEFAULT_HEADERS_TO_SIGN.add(Headers.YOP_ENCRYPT);
    }

    @Override
    public void sign(Request<? extends BaseRequest> request, YopCredentials<?> credentials, SignOptions options) {
        checkNotNull(request, "request should not be null.");
        if (credentials == null || credentials instanceof YopCredentialsWithoutSign) {
            return;
        }
        final Integer signExpirationInSeconds = request.getOriginalRequestObject().getRequestConfig().getSignExpirationInSeconds();
        if (null != signExpirationInSeconds && signExpirationInSeconds > 0) {
            options.setExpirationInSeconds(signExpirationInSeconds);
        }

        // A.构造认证字符串
        String authString = buildAuthString(credentials, options);
        LOGGER.debug("authString:{}", authString);

        // B.获取规范请求串
        additionalHeader(request, options);
        SortedMap<String, String> headersToSign = this.getHeadersToSign(request.getHeaders(), DEFAULT_HEADERS_TO_SIGN);
        String canonicalRequest = buildCanonicalRequest(request, authString, headersToSign);
        LOGGER.debug("canonicalRequest:{}", canonicalRequest.replace("\n", "[\\n]"));

        // C.计算签名
        CredentialsItem credentialsItem = (CredentialsItem) credentials.getCredential();
        YopSignProcessor yopSignProcessor = YopSignProcessorFactory.getSignProcessor(credentialsItem.getCertType().name());
        String signature = yopSignProcessor.sign(canonicalRequest, credentialsItem) + "$" + yopSignProcessor.getDigestAlg();
        LOGGER.debug("signature:{}", signature);

        // D.添加认证头
        String authorizationHeader = buildAuthzHeader(options, authString, headersToSign, signature);
        LOGGER.debug("Authorization:{}", authorizationHeader);
        request.addHeader(Headers.AUTHORIZATION, authorizationHeader);

        // E.添加签名序列号
        if (credentials instanceof CertificateCredentials) {
            request.addHeader(Headers.YOP_SIGN_CERT_SERIAL_NO, ((CertificateCredentials) credentials).getSerialNo());
        }
    }

    private void additionalHeader(Request<? extends BaseRequest> request, SignOptions options) {
        DigestAlgEnum digestAlg = options.getDigestAlg();
        String contentHash = calculateContentHash(request, digestAlg);
        request.addHeader(getDigestAlgHeaderName(digestAlg), contentHash);
    }

    private String buildAuthString(YopCredentials<?> credentials, SignOptions options) {
        String appKey = credentials.getAppKey();
        Date timestamp = new Date();
        return DEFAULT_YOP_PROTOCOL_VERSION + "/"
                + appKey + "/"
                + DateUtils.formatAlternateIso8601Date(timestamp) + "/"
                + options.getExpirationInSeconds();
    }

    private String buildCanonicalRequest(Request<? extends BaseRequest> request,
                                         String authString,
                                         SortedMap<String, String> headersToSign) {
        String canonicalQueryString = this.getCanonicalQueryString(request);
        String canonicalHeaders = this.getCanonicalHeaders(headersToSign);

        String apiUri = request.getResourcePath();
        String canonicalURI = this.getCanonicalURIPath(apiUri);
        return authString + "\n"
                + request.getHttpMethod() + "\n"
                + canonicalURI + "\n"
                + canonicalQueryString + "\n"
                + canonicalHeaders;
    }

    private String buildAuthzHeader(SignOptions options,
                                    String authString,
                                    SortedMap<String, String> headersToSign,
                                    String signature) {
        String signedHeaders = SIGNED_HEADER_STRING_JOINER.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();
        return options.getProtocolPrefix() + " " + authString + "/" + signedHeaders + "/" + signature;
    }

    private String getCanonicalQueryString(Request<? extends BaseRequest> request) {
        if (HttpUtils.usePayloadForQueryParameters(request)) {
            return "";
        }
        return HttpUtils.getCanonicalQueryString(request.getParameters(), true);
    }

    private String getDigestAlgHeaderName(DigestAlgEnum digestAlgEnum) {
        if (DigestAlgEnum.SM3 == digestAlgEnum) {
            return Headers.YOP_CONTENT_SM3;
        }
        return Headers.YOP_CONTENT_SHA256;
    }

    //TODO 请求重试时需要重新计算吗？
    private String calculateContentHash(Request<? extends BaseRequest> request, DigestAlgEnum digestAlg) {
        RestartableInputStream payloadStream = getBinaryRequestPayloadStream(request);
        String contentHash = Encodes.encodeHex(
                YopDigesterFactory.getDigester(digestAlg.name()).digest(payloadStream, digestAlg.name()));
        payloadStream.restart();
        return contentHash;
    }

    private RestartableInputStream getBinaryRequestPayloadStream(Request<? extends BaseRequest> request) {
        if (HttpUtils.usePayloadForQueryParameters(request)) {
            String encodedParameters = HttpUtils.getCanonicalQueryString(request.getParameters(), true);
            if (StringUtils.isEmpty(encodedParameters)) {
                return RestartableInputStream.wrap(new byte[0]);
            }

            return RestartableInputStream.wrap(
                    encodedParameters.getBytes(YopConstants.DEFAULT_CHARSET));
        }

        return getBinaryRequestPayloadStreamWithoutQueryParams(request);
    }

    private RestartableInputStream getBinaryRequestPayloadStreamWithoutQueryParams(Request<? extends BaseRequest> request) {
        if (request.getContent() instanceof RestartableInputStream) {
            return (RestartableInputStream) request.getContent();
        }
        return RestartableInputStream.wrap(new byte[0]);
    }

    private String getCanonicalURIPath(String path) {
        if (path == null) {
            return "/";
        } else if (path.startsWith("/")) {
            return HttpUtils.normalizePath(path);
        } else {
            return "/" + HttpUtils.normalizePath(path);
        }
    }

    private SortedMap<String, String> getHeadersToSign(Map<String, String> headers, Set<String> headersToSign) {
        SortedMap<String, String> ret = Maps.newTreeMap();
        if (headersToSign != null) {
            Set<String> tempSet = Sets.newHashSet();
            for (String header : headersToSign) {
                tempSet.add(header.trim().toLowerCase());
            }
            headersToSign = tempSet;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if ((headersToSign != null && headersToSign.contains(key.toLowerCase())
                        && !Headers.AUTHORIZATION.equalsIgnoreCase(key))) {
                    ret.put(key, entry.getValue());
                }
            }
        }
        return ret;
    }

    private String getCanonicalHeaders(SortedMap<String, String> headers) {
        if (headers.isEmpty()) {
            return "";
        }

        List<String> headerStrings = Lists.newArrayList();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                value = "";
            }
            headerStrings.add(HttpUtils.normalize(key.trim().toLowerCase()) + ':' + HttpUtils.normalize(value.trim()));
        }
        Collections.sort(headerStrings);

        return HEADER_JOINER.join(headerStrings);
    }



    @Override
    public List<String> supportSignerAlg() {
        return Lists.newArrayList(SignerTypeEnum.SM2.name(), SignerTypeEnum.RSA.name());
    }

}
