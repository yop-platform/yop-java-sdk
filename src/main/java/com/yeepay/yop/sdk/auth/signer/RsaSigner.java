package com.yeepay.yop.sdk.auth.signer;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.Signer;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopCredentialsWithoutSign;
import com.yeepay.yop.sdk.auth.credentials.YopRSACredentials;
import com.yeepay.yop.sdk.exception.VerifySignFailedException;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.internal.RestartableInputStream;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.rsa.RSA;
import com.yeepay.yop.sdk.utils.CharacterConstants;
import com.yeepay.yop.sdk.utils.DateUtils;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.*;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.yeepay.yop.sdk.utils.HttpUtils.isJsonContent;

/**
 * title: Rsa签名器<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 16:37
 */
public class RsaSigner implements Signer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RsaSigner.class);

    private static final ThreadLocal<MessageDigest> SHA256_MESSAGE_DIGEST;

    private static final String YOP_AUTH_VERSION = "yop-auth-v3";

    private static final String SEPARATOR = "$";

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

        SHA256_MESSAGE_DIGEST = new ThreadLocal<MessageDigest>() {
            @Override
            protected MessageDigest initialValue() {
                try {
                    return MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    throw new YopClientException(
                            "Unable to get SHA256 Function"
                                    + e.getMessage(), e);
                }
            }
        };
    }

    @Override
    public void sign(Request<? extends BaseRequest> request, YopCredentials credentials, SignOptions options) {
        checkNotNull(request, "request should not be null.");
        if (credentials == null || credentials instanceof YopCredentialsWithoutSign) {
            return;
        }
        if (!(credentials instanceof YopRSACredentials)) {
            throw new YopClientException("UnSupported credentials type:" + credentials.getClass().getSimpleName());
        }
        YopRSACredentials rsaCredentials = (YopRSACredentials) credentials;
        //TODO 校验rsaCredentials长度
        String accessKeyId = credentials.getAppKey();

        request.addHeader(Headers.HOST, HttpUtils.generateHostHeader(request.getEndpoint()));
        Date timestamp = new Date();

        String contentSha256 = calculateContentHash(request);
        request.addHeader(Headers.YOP_CONTENT_SHA256, contentSha256);
        // Formatting the query string with signing protocol.
        String canonicalQueryString = getCanonicalQueryString(request);
        // Sorted the headers should be signed from the request.
        SortedMap<String, String> headersToSign =
                this.getHeadersToSign(request.getHeaders(), defaultHeadersToSign);
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = this.getCanonicalHeaders(headersToSign);
        String signedHeaders = signedHeaderStringJoiner.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();

        String authString = YOP_AUTH_VERSION + "/" + accessKeyId + "/"
                + DateUtils.formatAlternateIso8601Date(timestamp) + "/" + options.getExpirationInSeconds();

        String apiUri = request.getResourcePath();
        String canonicalURI = this.getCanonicalURIPath(apiUri);
        String canonicalRequest = authString + "\n" + request.getHttpMethod() + "\n" + canonicalURI + "\n" +
                canonicalQueryString + "\n" + canonicalHeader;

        // Signing the canonical request using key with sha-256 algorithm.
        String signature = this.computeSignature(canonicalRequest, rsaCredentials.getPrivateKey(), options.getDigestAlg());

        String authorizationHeader = options.getProtocolPrefix() + " " + authString + "/" + signedHeaders + "/" + signature;

        LOGGER.debug("CanonicalRequest:{}\tAuthorization:{}", canonicalRequest.replace("\n", "[\\n]"),
                authorizationHeader);

        request.addHeader(Headers.AUTHORIZATION, authorizationHeader);

    }

    @Override
    public void checkSignature(YopHttpResponse httpResponse, String signature, PublicKey publicKey, SignOptions options) {
        String content = httpResponse.readContent();
        content = content.replaceAll("[ \t\n]", CharacterConstants.EMPTY);
        if (!RSA.verifySign(content, signature, publicKey, options.getDigestAlg())) {
            throw new VerifySignFailedException("response sign verify failure");
        }
    }

    private String getCanonicalQueryString(Request<? extends BaseRequest> request) {
        if (HttpUtils.usePayloadForQueryParameters(request)) {
            return "";
        }
        return HttpUtils.getCanonicalQueryString(request.getParameters(), true);
    }

    //TODO 请求重试时需要重新计算吗？
    private String calculateContentHash(Request<? extends BaseRequest> request) {
        RestartableInputStream payloadStream = getBinaryRequestPayloadStream(request);
        String contentSha256 = Encodes.encodeHex(hash(payloadStream));
        payloadStream.restart();
        return contentSha256;
    }

    private byte[] hash(InputStream input) {
        try {
            MessageDigest md = getMessageDigestInstance();
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
        if (request.getContent() instanceof RestartableInputStream
                && isJsonContent(request.getHeaders().get(Headers.CONTENT_TYPE))) {
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

    private String computeSignature(String content, PrivateKey signingKey, DigestAlgEnum digestAlg) {
        return RSA.sign(content, signingKey, digestAlg) + SEPARATOR + digestAlg.getValue();
    }

    /**
     * Returns the re-usable thread local version of MessageDigest.
     *
     * @return 摘要
     */
    private static MessageDigest getMessageDigestInstance() {
        MessageDigest messageDigest = SHA256_MESSAGE_DIGEST.get();
        messageDigest.reset();
        return messageDigest;
    }

}
