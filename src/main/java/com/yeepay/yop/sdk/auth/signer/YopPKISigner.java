/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopCredentialsWithoutSign;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessor;
import com.yeepay.yop.sdk.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.exception.YopClientException;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

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

    private static final ThreadLocal<Map<DigestAlgEnum, MessageDigest>> MESSAGE_DIGEST;

    private static final String YOP_AUTH_VERSION = "yop-auth-v3";

    private static final Set<String> defaultHeadersToSign = Sets.newHashSet();
    private static final Joiner headerJoiner = Joiner.on('\n');
    private static final Joiner signedHeaderStringJoiner = Joiner.on(';');

    static {
        defaultHeadersToSign.add(Headers.CONTENT_LENGTH.toLowerCase());
        defaultHeadersToSign.add(Headers.CONTENT_TYPE.toLowerCase());
        defaultHeadersToSign.add(Headers.CONTENT_MD5.toLowerCase());
        defaultHeadersToSign.add(Headers.YOP_REQUEST_ID);
        defaultHeadersToSign.add(Headers.YOP_DATE);
        defaultHeadersToSign.add(Headers.YOP_APPKEY);
        defaultHeadersToSign.add(Headers.YOP_CONTENT_SHA256);
        defaultHeadersToSign.add(Headers.YOP_HASH_CRC64ECMA);

        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        MESSAGE_DIGEST = new ThreadLocal<Map<DigestAlgEnum, MessageDigest>>() {
            @Override
            protected Map<DigestAlgEnum, MessageDigest> initialValue() {
                try {
                    Map<DigestAlgEnum, MessageDigest> messageDigestMap = new HashMap<>(3);
                    messageDigestMap.put(DigestAlgEnum.SM3, MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME));
                    messageDigestMap.put(DigestAlgEnum.SHA256, MessageDigest.getInstance("SHA-256"));
                    return messageDigestMap;
                } catch (GeneralSecurityException e) {
                    throw new YopClientException(
                            "Unable to get Digest Function"
                                    + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public List<String> supportSignerAlg() {
        return Lists.newArrayList(SignerTypeEnum.SM2.name(), SignerTypeEnum.RSA.name());
    }

    @Override
    public void sign(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options) {
        checkNotNull(request, "request should not be null.");
        if (credentials == null || credentials instanceof YopCredentialsWithoutSign) {
            return;
        }
        CredentialsItem credentialsItem = (CredentialsItem) credentials.getCredential();

        // 添额外的请求头
        additionalHeader(request, options);

        // Sorted the headers should be signed from the request.
        SortedMap<String, String> headersToSign = this.getHeadersToSign(request.getHeaders(), defaultHeadersToSign);

        // 获取规范请求串
        String canonicalRequest = buildCanonicalRequest(request, credentials, options, headersToSign);

        // 计算签名
        YopSignProcessor yopSignProcessor = YopSignProcessorFactory.getSignProcessor(credentialsItem.getCertType().name());
        String signature = yopSignProcessor.sign(canonicalRequest, credentialsItem) + "$" + yopSignProcessor.getDigestAlg();

        LOGGER.debug("CanonicalRequest:{}", canonicalRequest.replace("\n", "[\\n]"));
        // 添加认证头
        addAuthHeader(request, credentials, options, headersToSign, signature);
    }

    private void additionalHeader(Request<? extends BaseRequest> request, SignOptions options) {
        DigestAlgEnum digestAlg = options.getDigestAlg();
        request.addHeader(Headers.HOST, HttpUtils.generateHostHeader(request.getEndpoint()));
        String contentHash = calculateContentHash(request, digestAlg);
        request.addHeader(getHeader(digestAlg), contentHash);
    }


    private void addAuthHeader(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options,
                               SortedMap<String, String> headersToSign, String signature) {
        String accessKeyId = credentials.getAppKey();
        Date timestamp = new Date();
        String authString = YOP_AUTH_VERSION + "/" + accessKeyId + "/"
                + DateUtils.formatAlternateIso8601Date(timestamp) + "/" + options.getExpirationInSeconds();
        String signedHeaders = signedHeaderStringJoiner.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();
        String authorizationHeader = options.getProtocolPrefix() + " " + authString + "/" + signedHeaders + "/" + signature;

        LOGGER.debug("Authorization:{}", authorizationHeader);

        request.addHeader(Headers.AUTHORIZATION, authorizationHeader);
    }


    private String buildCanonicalRequest(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options,
                                         SortedMap<String, String> headersToSign) {
        //TODO 校验rsaCredentials长度
        String accessKeyId = credentials.getAppKey();

        Date timestamp = new Date();
        // Formatting the query string with signing protocol.
        String canonicalQueryString = getCanonicalQueryString(request);
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = this.getCanonicalHeaders(headersToSign);

        String authString = YOP_AUTH_VERSION + "/" + accessKeyId + "/"
                + DateUtils.formatAlternateIso8601Date(timestamp) + "/" + options.getExpirationInSeconds();

        String apiUri = request.getResourcePath();
        String canonicalURI = this.getCanonicalURIPath(apiUri);
        return authString + "\n" + request.getHttpMethod() + "\n" + canonicalURI + "\n" +
                canonicalQueryString + "\n" + canonicalHeader;
    }

    private String getCanonicalQueryString(Request<? extends BaseRequest> request) {
        if (HttpUtils.usePayloadForQueryParameters(request)) {
            return "";
        }
        return HttpUtils.getCanonicalQueryString(request.getParameters(), true);
    }

    private String getHeader(DigestAlgEnum digestAlgEnum) {
        if (DigestAlgEnum.SM3 == digestAlgEnum) {
            return Headers.YOP_CONTENT_SM3;
        }
        return Headers.YOP_CONTENT_SHA256;
    }

    //TODO 请求重试时需要重新计算吗？
    private String calculateContentHash(Request<? extends BaseRequest> request, DigestAlgEnum digestAlg) {
        RestartableInputStream payloadStream = getBinaryRequestPayloadStream(request);
        String contentHash = Encodes.encodeHex(hash(payloadStream, digestAlg));
        payloadStream.restart();
        return contentHash;
    }

    private byte[] hash(InputStream input, DigestAlgEnum digestAlg) {
        try {
            MessageDigest md = getMessageDigestInstance(digestAlg);
            DigestInputStream digestInputStream = new DigestInputStream(
                    input, md);
            byte[] buffer = new byte[1024];
            while (digestInputStream.read(buffer) > -1) {
            }
            return digestInputStream.getMessageDigest().digest();
        } catch (Exception e) {
            throw new YopClientException(
                    "Unable to compute hash while signing request: "
                            + e.getMessage(), e);
        }
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

        return headerJoiner.join(headerStrings);
    }

    /**
     * Returns the re-usable thread local version of MessageDigest.
     *
     * @return 摘要
     */
    private static MessageDigest getMessageDigestInstance(DigestAlgEnum digestAlg) {
        MessageDigest messageDigest = MESSAGE_DIGEST.get().get(digestAlg);
        messageDigest.reset();
        return messageDigest;
    }

}
