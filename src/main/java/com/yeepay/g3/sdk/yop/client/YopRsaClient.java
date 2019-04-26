package com.yeepay.g3.sdk.yop.client;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.yeepay.g3.sdk.yop.encrypt.Base64;
import com.yeepay.g3.sdk.yop.encrypt.*;
import com.yeepay.g3.sdk.yop.exception.YopClientException;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpMethodName;
import com.yeepay.g3.sdk.yop.http.HttpUtils;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.*;
import com.yeepay.g3.sdk.yop.utils.checksum.CRC64Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.zip.CheckedInputStream;

/**
 * <pre>
 * 非对称 Client，简化用户发起请求及解析结果的处理
 * </pre>
 *
 * @author baitao.ji
 * @version 3.0
 */
public class YopRsaClient extends AbstractClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(YopRsaClient.class);

    private static final Set<String> defaultHeadersToSign = Sets.newHashSet();
    private static final Joiner headerJoiner = Joiner.on('\n');
    private static final Joiner signedHeaderStringJoiner = Joiner.on(';');

    private static final String EXPIRED_SECONDS = "1800";

    static {
//        defaultHeadersToSign.add(Headers.HOST.toLowerCase());
//        defaultHeadersToSign.add(Headers.CONTENT_LENGTH.toLowerCase());
//        defaultHeadersToSign.add(Headers.CONTENT_TYPE.toLowerCase());
//        defaultHeadersToSign.add(Headers.CONTENT_MD5.toLowerCase());
//        defaultHeadersToSign.add(Headers.YOP_HASH_CRC64ECMA.toLowerCase());
    }

    public static YopResponse get(String apiUri, YopRequest request) throws IOException {
        return handleNormalRequest(apiUri, request, HttpMethodName.GET);
    }

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String apiUri, YopRequest request) throws IOException {
        return handleNormalRequest(apiUri, request, HttpMethodName.POST);
    }

    private static YopResponse handleNormalRequest(String apiUri, YopRequest request, HttpMethodName method) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, method);
        String contentUrl = richRequest(apiUri, request);
        HttpUriRequest httpPost = buildFormHttpRequest(request, contentUrl, method);
        YopResponse response = fetchContentByApacheHttpClient(httpPost, new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        return response;
    }

    private static void encryptRequest(YopRequest request) {
        String encryptKey = StringUtils.defaultIfBlank(request.getEncryptKey(), request.getAppSdkConfig().getEncryptKey());
        if (StringUtils.isBlank(encryptKey)) {
            throw new YopClientException("no encryptKey configured");
        }
        request.addHeader(Headers.YOP_ENCRYPT_TYPE, getEncryptType(encryptKey));
        //加密密钥回写，用于接下来构造ResponseConfig信息
        request.setEncryptKey(encryptKey);

        //参数值加密
        if (request.getParams() != null) {
            Multimap<String, String> paramMultiMap = request.getParams();
            for (String key : paramMultiMap.keySet()) {
                Collection<String> values = paramMultiMap.get(key);
                Collection<String> encryptedValues = new ArrayList<String>(values.size());
                for (String value : values) {
                    encryptedValues.add(AESEncrypter.encrypt(value, encryptKey));
                }
                paramMultiMap.replaceValues(key, encryptedValues);
            }
        }
    }

    private static String getEncryptType(String encryptKey) {
        byte[] decoded = Base64.decode(encryptKey.getBytes());
        if (decoded.length == 16 || decoded.length == 32) {
            return "aes" + decoded.length * 8;
        } else {
            throw new YopClientException("unsupported encryptKey length");
        }
    }

    /**
     * 上传文件
     *
     * @param apiUri  目标地址或命名模式的method
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse upload(String apiUri, YopRequest request) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        if (BooleanUtils.isTrue(request.isNeedEncrypt())) {
            encryptRequest(request);
        }
        sign(apiUri, request, HttpMethodName.POST);
        String contentUrl = richRequest(apiUri, request);
        Pair<HttpUriRequest, List<CheckedInputStream>> pair = buildMultiFormRequest(request, contentUrl);
        YopResponse response = fetchContentByApacheHttpClient(pair.getLeft(), new ResponseConfig()
                .withNeedEncrypt(request.isNeedEncrypt())
                .withEncryptKey(request.getEncryptKey())
                .withYopPublicKey(InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
        handleRsaResult(response);
        if (pair.getRight() != null) {
            checkFileIntegrity(response, CRC64Utils.getCRC64(pair.getRight()));
        }
        return response;
    }

    private static void sign(String apiUri, YopRequest request, HttpMethodName httpMethod) {
        String appKey = request.getAppSdkConfig().getAppKey();
        String timestamp = DateUtils.formatCompressedIso8601Timestamp(System.currentTimeMillis());

//        authorization  yop-auth-v2/openSmsApi/2016-02-25T08:57:48Z/1800/host/a57365cb4bf6cd83c91dfae214c1404aa0cc74f2ade95f121530fcb9c91f3c9d

        Map<String, String> headers = request.getHeaders();
        headers.put(Headers.YOP_SESSION_ID, SESSION_ID);
        headers.put(Headers.YOP_REQUEST_ID, getUUID());

        Set<String> headersToSignSet = new HashSet<String>();
        headersToSignSet.add(Headers.YOP_REQUEST_ID);

        String authString = InternalConfig.PROTOCOL_VERSION + "/" + appKey + "/" + timestamp + "/" + EXPIRED_SECONDS;

        // Formatting the URL with signing protocol.
        String canonicalURI = HttpUtils.getCanonicalURIPath(apiUri);
        // Formatting the query string with signing protocol.
        String canonicalQueryString = HttpUtils.getCanonicalQueryString(request.getParams(), true);
        // Sorted the headers should be signed from the request.
        SortedMap<String, String> headersToSign = getHeadersToSign(headers, headersToSignSet);
        // Formatting the headers from the request based on signing protocol.
        String canonicalHeader = getCanonicalHeaders(headersToSign);
        String signedHeaders = signedHeaderStringJoiner.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();

        String canonicalRequest = authString + "\n" + httpMethod + "\n" + canonicalURI + "\n" + canonicalQueryString + "\n" + canonicalHeader;

        // Signing the canonical request using key with sha-256 algorithm.

        PrivateKey isvPrivateKey;
        if (StringUtils.length(request.getSecretKey()) > 128) {
            try {
                isvPrivateKey = RSAKeyUtils.string2PrivateKey(request.getSecretKey());
            } catch (NoSuchAlgorithmException e) {
                throw Exceptions.unchecked(e);
            } catch (InvalidKeySpecException e) {
                throw Exceptions.unchecked(e);
            }
        } else {
            isvPrivateKey = request.getAppSdkConfig().getDefaultIsvPrivateKey();
        }
        if (null == isvPrivateKey) {
            throw new YopClientException("Can't init ISV private key!");
        }

        DigitalSignatureDTO digitalSignatureDTO = new DigitalSignatureDTO();
        digitalSignatureDTO.setPlainText(canonicalRequest);
        digitalSignatureDTO.setCertType(CertTypeEnum.RSA2048);
        digitalSignatureDTO.setDigestAlg(DigestAlgEnum.SHA256);
        digitalSignatureDTO = DigitalEnvelopeUtils.sign(digitalSignatureDTO, isvPrivateKey);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("canonicalRequest:" + canonicalRequest);
            LOGGER.debug("signature:" + digitalSignatureDTO.getSignature());
        }

        headers.put(Headers.AUTHORIZATION, "YOP-RSA2048-SHA256 " + InternalConfig.PROTOCOL_VERSION + "/" + appKey + "/" + timestamp + "/" + EXPIRED_SECONDS + "/" + signedHeaders + "/" + digitalSignatureDTO.getSignature());
    }

    private static void handleRsaResult(YopResponse response) {
        String stringResult = response.getStringResult();
        if (StringUtils.isNotBlank(stringResult)) {
            response.setResult(JacksonJsonMarshaller.unmarshal(stringResult, Object.class));
        }
    }

    private static String getCanonicalHeaders(SortedMap<String, String> headers) {
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

    private static SortedMap<String, String> getHeadersToSign(Map<String, String> headers, Set<String> headersToSign) {
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
                        && !Headers.AUTHORIZATION.equalsIgnoreCase(key))
                        || (headersToSign == null && isDefaultHeaderToSign(key))) {
                    ret.put(key, entry.getValue());
                }
            }
        }
        return ret;
    }

    private static boolean isDefaultHeaderToSign(String header) {
        header = header.trim().toLowerCase();
        return header.startsWith(Headers.YOP_PREFIX) || defaultHeadersToSign.contains(header);
    }

}
