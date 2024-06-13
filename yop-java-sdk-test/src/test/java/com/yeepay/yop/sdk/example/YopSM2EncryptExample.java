/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: Sm2 加密示例<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/9/27
 */
public class YopSM2EncryptExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSM2EncryptExample.class);
    static {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            LOGGER.debug("BouncyCastleProvider added");
        } catch (Exception e) {
            LOGGER.warn("error when add BouncyCastleProvider", e);
        }
    }

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final String APP_KEY = "sandbox_sm_10080041523";
    private static final BCECPrivateKey ISV_PRIVATE_KEY = string2PrivateKey("MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgr0mQ3/jjQOczWI6bnJFdqF4D/DFYHaqXftqXU/jGKpCgCgYIKoEcz1UBgi2hRANCAAQPpkZNnOnXTCXOIHJbfR+i6ea1QkM8HxkdO8KSWK8IgltHZxr5xlxiqR8inOREmmrxUQQagOH5i3oELWgXZz8G");
    private static final String YOP_PUBLIC_KEY_SERIAL_NO = "4052988765";
    private static final BCECPublicKey YOP_PUBLIC_KEY = string2PublicKey("MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAE3OYUx7xC7Pzy6XFNDBAqte2tKKr/kn4N9duYeTuaSieslZD0hMoHPI3UWpni/fkpgtcluICawo7RRGtn8plSfw==");
    private static final String SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";
    private static final String SLASH = "/";
    private static final String UNDER_LINE = "_";
    private static final String SEMICOLON = ";";
    private static final String EMPTY = "";

    private static final String SM4_CBC_PKCS5PADDING = "SM4/CBC/PKCS5Padding";
    private static final byte[] SM4_IV = new SecureRandom().generateSeed(16);
    private static final String STREAM = "stream";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String DEFAULT_YOP_PROTOCOL_VERSION = "yop-auth-v3";
    public static final String DEFAULT_AUTH_PREFIX_SM2 = "YOP-SM2-SM3";
    private static final String DEFAULT_DIGEST_ALG = "SM3";
    private static final Joiner QUERY_STRING_JOINER = Joiner.on('&');

    // mode 指定密文结构，旧标准的为C1C2C3，新的[《SM2密码算法使用规范》 GM/T 0009-2012]标准为C1C3C2
    // 我们采用C1C3C2
    // 根据mode不同，输出的密文C1C2C3排列顺序不同。C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
    private static final ThreadLocal<SM2Engine> engineThreadLocal = new ThreadLocal<SM2Engine>() {
        @Override
        protected SM2Engine initialValue() {
            return new SM2Engine(SM2Engine.Mode.C1C3C2);
        }
    };

    public static void main(String[] args) throws Exception {
        // 加密会话密钥，可每笔调用都生成，也可以多笔公用一个，建议定时更换
        String encryptKey = encodeUrlSafeBase64(generateRandomKey());

//        // get请求，form参数
        getFormExample(YopRequestMethod.GET, "/rest/v1.0/test/errorcode2",
                YopRequestContentType.FORM_URL_ENCODE, encryptKey);

        // post请求，form参数
        postFormExample(YopRequestMethod.POST, "/rest/v1.0/test/old-api-mgr/find-api-by-uri",
                YopRequestContentType.FORM_URL_ENCODE, encryptKey);

        // post请求，json参数
        postJsonExample(YopRequestMethod.POST, "/rest/v1.0/test-wdc/test/http-json/test",
                YopRequestContentType.JSON, encryptKey);

        // get请求，form参数，下载文件
        downloadExample(YopRequestMethod.GET, "/yos/v1.0/test/test/ceph-download",
                YopRequestContentType.FORM_URL_ENCODE, encryptKey);
    }

    private static void getFormExample(YopRequestMethod requestMethod, String requestUri,
                                       YopRequestContentType requestContentType,
                                       String encryptKey) throws Exception {
        // 请求参数
        String paramKey = "errorCode";
        String paramPlainValue = "000027",
                paramEncryptValue = encryptParam(encryptKey, paramPlainValue);
        Multimap<String, String> formParams = ArrayListMultimap.create();
        // 参数不加密
//        formParams.put(paramKey, paramPlainValue);

        // 参数加密
        formParams.put(paramKey, paramEncryptValue);
        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Sets.newHashSet();
        encryptParams.add(paramKey);

        // 请求头
        Map<String, String> headers = buildHeaders(requestMethod, requestUri, formParams,
                requestContentType, "", encryptKey, encryptHeaders, encryptParams);

        // 构造http请求
        HttpUriRequest request = buildHttpRequest(SERVER_ROOT + requestUri,
                requestMethod, headers, formParams, null);

        // 发起http调用
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            handleResponse(YopRequestType.WEB, response, encryptKey);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static Map<String, String> buildHeaders(YopRequestMethod httpMethod, String apiUri,
                                                    Multimap<String, String> params,
                                                    YopRequestContentType contentType, String content,
                                                    String encryptKey, Set<String> encryptHeaders, Set<String> encryptParams) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(YOP_SDK_LANGS, "java");
        headers.put(YOP_SDK_VERSION, "4.3.0");
        headers.put(YOP_APPKEY, APP_KEY);
        headers.put(YOP_REQUEST_ID, UUID.randomUUID().toString());

        // 加密头：请求参数不加密的情况, 也需设置加密头，用于响应结果加密
        headers.put(YOP_ENCRYPT, buildEncryptHeader(encryptHeaders, encryptParams, encryptKey));

        // 摘要头
        headers.put(YOP_CONTENT_SM3, calculateContentHash(httpMethod, contentType, params, content));

        // 签名头
        headers.put(AUTHORIZATION, signRequest(httpMethod, apiUri, headers, params));
        return headers;
    }

    private static void postFormExample(YopRequestMethod requestMethod, String requestUri,
                                        YopRequestContentType requestContentType,
                                        String encryptKey) throws Exception {

        // 请求参数
        String paramKey = "apiUri";
        String paramPlainValue = "/rest/v1.0/test/product/find/lookatdoc",
                paramEncryptValue = encryptParam(encryptKey, paramPlainValue);
        Multimap<String, String> params = ArrayListMultimap.create();
        // 参数不加密
//        params.put(paramKey, paramPlainValue);

        // 参数加密
        params.put(paramKey, paramEncryptValue);
        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Sets.newHashSet();
        encryptParams.add(paramKey);

        // 请求头
        Map<String, String> headers = buildHeaders(requestMethod, requestUri, params,
                requestContentType, "", encryptKey, encryptHeaders, encryptParams);

        // 构造http请求
        HttpUriRequest request = buildHttpRequest(SERVER_ROOT + requestUri,
                requestMethod, headers, params, null);

        // 发起http调用
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            handleResponse(YopRequestType.WEB, response, encryptKey);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static void postJsonExample(YopRequestMethod requestMethod, String requestUri,
                                        YopRequestContentType requestContentType,
                                        String encryptKey) throws Exception {
        // 请求参数
        String plainJsonContent = "{\n" +
                "  \"arg1\" : {\n" +
                "    \"appId\" : \"app_1111111111\",\n" +
                "    \"customerNo\" : \"333333333\"\n" +
                "  },\n" +
                "  \"arg0\" : {\n" +
                "    \"string\" : \"hello\",\n" +
                "    \"array\" : [ \"test\" ]\n" +
                "  }\n" +
                "}",
                encryptJsonContent = encryptParam(encryptKey, plainJsonContent);

        String finalJsonContent
        // 不加密
//                = plainJsonContent;
        // 加密
              = encryptJsonContent;
        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Sets.newHashSet();
        encryptParams.add("$");// 目前json推荐整体加密

        // 请求头
        // 目前不允许json接口带有form参数，置空即可
        final ArrayListMultimap<String, String> params = ArrayListMultimap.create();
        Map<String, String> headers = buildHeaders(requestMethod, requestUri, params,
                requestContentType, finalJsonContent, encryptKey, encryptHeaders, encryptParams);

        // 构造http请求
        HttpUriRequest request = buildHttpRequest(SERVER_ROOT + requestUri,
                requestMethod, headers, params, finalJsonContent);

        // 发起http调用
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            handleResponse(YopRequestType.WEB, response, encryptKey);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static void downloadExample(YopRequestMethod requestMethod, String requestUri,
                                        YopRequestContentType requestContentType,
                                        String encryptKey) throws Exception {
        // 根据实际情况来，也可能是post方法，请求报文可能是form，也可能是json，可参考其他方式的入参处理
        // 此处仅演示文件响应流的处理
        // 请求参数
        String paramKey = "fileName";
        String paramPlainValue = "wym-test.txt",
                paramEncryptValue = encryptParam(encryptKey, paramPlainValue);
        Multimap<String, String> params = ArrayListMultimap.create();
        // 不加密
//        params.put(paramKey, paramPlainValue);

        // 加密
        params.put(paramKey, paramEncryptValue);
        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Sets.newHashSet();
        encryptParams.add(paramKey);

        // 请求头
        Map<String, String> headers = buildHeaders(requestMethod, requestUri, params,
                requestContentType, "", encryptKey, encryptHeaders, encryptParams);

        // 构造http请求
        HttpUriRequest request = buildHttpRequest(SERVER_ROOT + requestUri,
                requestMethod, headers, params, "");

        // 发起请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            handleResponse(YopRequestType.FILE_DOWNLOAD, response, encryptKey);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static final Set<String> DEFAULT_HEADERS_TO_SIGN = Sets.newHashSet();
    private static final Joiner HEADER_JOINER = Joiner.on('\n');
    private static final Joiner SIGNED_HEADER_STRING_JOINER = Joiner.on(';');

    private static final String YOP_SDK_VERSION = "x-yop-sdk-version";
    private static final String YOP_SDK_LANGS = "x-yop-sdk-langs";
    private static final String YOP_REQUEST_ID = "x-yop-request-id";
    private static final String YOP_APPKEY = "x-yop-appkey";
    private static final String YOP_CONTENT_SM3 = "x-yop-content-sm3";
    private static final String YOP_ENCRYPT = "x-yop-encrypt";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_TYPE = "Content-Type";

    static {
        DEFAULT_HEADERS_TO_SIGN.add(YOP_REQUEST_ID);
        DEFAULT_HEADERS_TO_SIGN.add(YOP_APPKEY);
        DEFAULT_HEADERS_TO_SIGN.add(YOP_CONTENT_SM3);
        DEFAULT_HEADERS_TO_SIGN.add(YOP_ENCRYPT);
    }

    private static String signRequest(YopRequestMethod requestMethod, String requestUri,
                                      Map<String, String> headers, Multimap<String, String> params) throws Exception {
        // A.构造认证字符串
        String authString = buildAuthString();

        // B.获取规范请求串
        SortedMap<String, String> headersToSign = getHeadersToSign(headers, DEFAULT_HEADERS_TO_SIGN);
        String canonicalRequest = buildCanonicalRequest(requestMethod, requestUri, params, authString, headersToSign);

        // C.计算签名
        String signature = encodeUrlSafeBase64(sign(canonicalRequest.getBytes(DEFAULT_ENCODING))) + "$" + DEFAULT_DIGEST_ALG;

        // D.添加认证头
        return buildAuthHeader(authString, headersToSign, signature);
    }

    private static String buildAuthHeader(String authString,
                                          SortedMap<String, String> headersToSign,
                                          String signature) {
        String signedHeaders = SIGNED_HEADER_STRING_JOINER.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();
        return DEFAULT_AUTH_PREFIX_SM2 + " " + authString + "/" + signedHeaders + "/" + signature;
    }

    // SM2
    public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
    public final static BigInteger SM2_ECC_N = CURVE.getOrder();
    public final static BigInteger SM2_ECC_H = CURVE.getCofactor();
    public final static BigInteger SM2_ECC_GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    public final static BigInteger SM2_ECC_GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);
    public static final ECPoint G_POINT = CURVE.createPoint(SM2_ECC_GX, SM2_ECC_GY);
    public static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT,
            SM2_ECC_N, SM2_ECC_H);
    public static final int CURVE_LEN = getCurveLength(DOMAIN_PARAMS);
    public static int getCurveLength(ECDomainParameters domainParams) {
        return (domainParams.getCurve().getFieldSize() + 7) / 8;
    }

    private static byte[] sign(byte[] data) {
        try {
            ECParameterSpec parameterSpec = ISV_PRIVATE_KEY.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPrivateKeyParameters priKeyParameters = new ECPrivateKeyParameters(ISV_PRIVATE_KEY.getD(), domainParameters);
            //der编码后的签名值
            byte[] derSign = sign(priKeyParameters, null, data);

            //der解码过程
            ASN1Sequence as = DERSequence.getInstance(derSign);
            byte[] rBytes = ((ASN1Integer) as.getObjectAt(0)).getValue().toByteArray();
            byte[] sBytes = ((ASN1Integer) as.getObjectAt(1)).getValue().toByteArray();
            //由于大数的补0规则，所以可能会出现33个字节的情况，要修正回32个字节
            rBytes = fixToCurveLengthBytes(rBytes);
            sBytes = fixToCurveLengthBytes(sBytes);
            byte[] rawSign = new byte[rBytes.length + sBytes.length];
            System.arraycopy(rBytes, 0, rawSign, 0, rBytes.length);
            System.arraycopy(sBytes, 0, rawSign, rBytes.length, sBytes.length);
            return rawSign;
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Sign Fail, key:" + ISV_PRIVATE_KEY + ", ex:", e);
        }
    }

    private static byte[] fixToCurveLengthBytes(byte[] src) {
        if (src.length == CURVE_LEN) {
            return src;
        }

        byte[] result = new byte[CURVE_LEN];
        if (src.length > CURVE_LEN) {
            System.arraycopy(src, src.length - result.length, result, 0, result.length);
        } else {
            System.arraycopy(src, 0, result, result.length - src.length, src.length);
        }
        return result;
    }

    /**
     * 签名
     *
     * @param priKeyParameters 私钥
     * @param withId           可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @param srcData          源数据
     * @return DER编码后的签名值
     * @throws CryptoException
     */
    public static byte[] sign(ECPrivateKeyParameters priKeyParameters, byte[] withId, byte[] srcData)
            throws CryptoException {
        SM2Signer signer = new SM2Signer();
        CipherParameters param;
        ParametersWithRandom pwr = new ParametersWithRandom(priKeyParameters, new SecureRandom());
        if (withId != null) {
            param = new ParametersWithID(pwr, withId);
        } else {
            param = pwr;
        }
        signer.init(true, param);
        signer.update(srcData, 0, srcData.length);
        return signer.generateSignature();
    }

    private static BCECPrivateKey string2PrivateKey(String priKey) {
        try {
            return (BCECPrivateKey)KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME).generatePrivate(
                    new PKCS8EncodedKeySpec(decodeBase64(priKey)));
        } catch (Exception e) {
            throw new RuntimeException("ConfigProblem, IsvPrivateKey ParseFail, value:" + priKey + ", ex:", e);
        }
    }

    private static byte[] decodeBase64(String input) {
        return Base64.decodeBase64(input);
    }

    private static String buildCanonicalRequest(YopRequestMethod httpMethod, String apiUri, Multimap<String, String> params,
                                                String authString,
                                                SortedMap<String, String> headersToSign) {
        String canonicalQueryString;
        if (YopRequestMethod.GET.equals(httpMethod) && null != params) {
            canonicalQueryString = getCanonicalQueryString(params, true);
        } else {
            canonicalQueryString = "";// post from 与json时，均为空，此处先简单处理
        }
        String canonicalHeaders = getCanonicalHeaders(headersToSign);

        String canonicalURI = getCanonicalURIPath(apiUri);
        return authString + "\n"
                + httpMethod.name() + "\n"
                + canonicalURI + "\n"
                + canonicalQueryString + "\n"
                + canonicalHeaders;
    }

    private static String getCanonicalURIPath(String path) {
        if (path == null) {
            return "/";
        } else if (path.startsWith("/")) {
            return normalizePath(path);
        } else {
            return "/" + normalizePath(path);
        }
    }

    private static String normalizePath(String path) {
        return normalize(path).replace("%2F", "/");
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
                        && !AUTHORIZATION.equalsIgnoreCase(key))) {
                    ret.put(key, entry.getValue());
                }
            }
        }
        return ret;
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
            headerStrings.add(normalize(key.trim().toLowerCase()) + ':' + normalize(value.trim()));
        }
        Collections.sort(headerStrings);

        return HEADER_JOINER.join(headerStrings);
    }

    private static final DateTimeFormatter alternateIso8601DateFormat =
            ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

    private static String buildAuthString() {
        Date timestamp = new Date();
        return DEFAULT_YOP_PROTOCOL_VERSION + "/"
                + APP_KEY + "/"
                + alternateIso8601DateFormat.print(new DateTime(timestamp)) + "/"
                + "1800";
    }

    private static String calculateContentHash(String jsonContent) throws Exception {
        InputStream contentStream = getContentStream(jsonContent);
        return Hex.encodeHexString((digest(contentStream)));
    }

    private static String calculateContentHash(YopRequestMethod requestMethod, YopRequestContentType requestContentType,
                                               Multimap<String, String> params, String content) throws Exception {
        String digestSource;

        if (requestMethod.equals(YopRequestMethod.GET)) {
            digestSource = "";
        } else if (requestMethod.equals(YopRequestMethod.POST)
                && requestContentType.equals(YopRequestContentType.JSON)) {
            digestSource = content;
        } else {
            digestSource = getCanonicalQueryString(params, true);
        }
        InputStream contentStream = getContentStream(digestSource);
        return Hex.encodeHexString((digest(contentStream)));
    }

    private static ByteArrayInputStream getContentStream(Multimap<String, String> params) throws Exception {
        return getContentStream(getCanonicalQueryString(params, true));
    }

    private static ByteArrayInputStream getContentStream(String paramStr) throws Exception {
        byte[] bytes;
        if (StringUtils.isEmpty(paramStr)) {
            bytes = new byte[0];
        } else {
            bytes = paramStr.getBytes(DEFAULT_ENCODING);
        }
        return new ByteArrayInputStream(bytes);
    }

    public static String getCanonicalQueryString(Multimap<String, String> params, boolean forSignature) {
        Map<String, Collection<String>> parameters;
        if (null == params || (parameters = params.asMap()).isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, Collection<String>> entry : parameters.entrySet()) {
            if (forSignature && AUTHORIZATION.equalsIgnoreCase(entry.getKey())) {
                continue;
            }
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");
            Collection<String> value = entry.getValue();
            if (value == null) {
                if (forSignature) {
                    parameterStrings.add(normalize(key) + '=');
                } else {
                    parameterStrings.add(normalize(key));
                }
            } else {
                for (String item : value) {
                    parameterStrings.add(normalize(key) + '=' + normalize(item));
                }
            }
        }
        Collections.sort(parameterStrings);

        return QUERY_STRING_JOINER.join(parameterStrings);
    }

    private static final BitSet URI_UNRESERVED_CHARACTERS = new BitSet();
    private static final String[] PERCENT_ENCODED_STRINGS = new String[256];

    static {
        /*
         * StringBuilder pattern = new StringBuilder();
         *
         * pattern .append(Pattern.quote("+")) .append("|") .append(Pattern.quote("*")) .append("|")
         * .append(Pattern.quote("%7E")) .append("|") .append(Pattern.quote("%2F"));
         *
         * ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
         */
        for (int i = 'a'; i <= 'z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        URI_UNRESERVED_CHARACTERS.set('-');
        URI_UNRESERVED_CHARACTERS.set('.');
        URI_UNRESERVED_CHARACTERS.set('_');
        URI_UNRESERVED_CHARACTERS.set('~');

        for (int i = 0; i < PERCENT_ENCODED_STRINGS.length; ++i) {
            PERCENT_ENCODED_STRINGS[i] = String.format("%%%02X", i);
        }
    }

    public static String normalize(String value) {
        try {
            StringBuilder builder = new StringBuilder();
            for (byte b : value.getBytes(DEFAULT_ENCODING)) {
                if (URI_UNRESERVED_CHARACTERS.get(b & 0xFF)) {
                    builder.append((char) b);
                } else {
                    builder.append(PERCENT_ENCODED_STRINGS[b & 0xFF]);
                }
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] digest(InputStream input) {
        try {
            MessageDigest md = MessageDigest.getInstance(DEFAULT_DIGEST_ALG, BouncyCastleProvider.PROVIDER_NAME);
            DigestInputStream digestInputStream = new DigestInputStream(input, md);
            byte[] buffer = new byte[1024];
            while (digestInputStream.read(buffer) > -1) {}
            return digestInputStream.getMessageDigest().digest();
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Digest Fail, alg:" + DEFAULT_DIGEST_ALG + ", ex:", e);
        }
    }

    /**
     * 加密协议头(请求)：
     *
     * yop-encrypt-v1/{服务端证书序列号}/{密钥类型(必填)}_{分组模式(必填)}_{填充算法(必填)}/{加密密钥值(必填)}/{IV}{;}{附加信息}/{客户端支持的大参数加密模式(必填)}/{encryptHeaders}/{encryptParams}
     */
    public static String buildEncryptHeader(Set<String> encryptHeaders, Set<String> encryptParams,
                                            String sm4Key) throws Exception {

        return  "yop-encrypt-v1" + SLASH +
                YOP_PUBLIC_KEY_SERIAL_NO + SLASH + //平台SM2证书序列号
                StringUtils.replace(SM4_CBC_PKCS5PADDING, SLASH, UNDER_LINE) + SLASH +
                encodeUrlSafeBase64(encryptKey(decodeBase64(sm4Key))) + SLASH +
                encodeUrlSafeBase64(SM4_IV) + SEMICOLON + EMPTY + SLASH +
                STREAM + SLASH +
                StringUtils.join(encryptHeaders, SEMICOLON) + SLASH +
                encodeUrlSafeBase64(StringUtils.join(encryptParams, SEMICOLON).getBytes(DEFAULT_ENCODING));
    }

    public static BCECPublicKey string2PublicKey(String pubKey) {
        try {
            return (BCECPublicKey) KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(decodeBase64(pubKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("ConfigProblem, YopPublicKey ParseFail, value:" + pubKey + ", ex:", e);
        }
    }

    private static byte[] encryptKey(byte[] sm4Key) {
        try {
            SM2Engine engine = engineThreadLocal.get();
            ECPublicKeyParameters pubKeyParameters = convertPublicKeyToParameters();
            ParametersWithRandom pwr = new ParametersWithRandom(pubKeyParameters, new SecureRandom());
            engine.init(true, pwr);
            return engine.processBlock(sm4Key, 0, sm4Key.length);
        } catch (Throwable e) {
            throw new RuntimeException("SystemError, Encrypt Fail, publicKey:" + YOP_PUBLIC_KEY, e);
        }
    }

    private static ECPublicKeyParameters convertPublicKeyToParameters() {
        ECParameterSpec parameterSpec = YOP_PUBLIC_KEY.getParameters();
        ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                parameterSpec.getN(), parameterSpec.getH());
        return new ECPublicKeyParameters(YOP_PUBLIC_KEY.getQ(), domainParameters);
    }

    private static String encryptParam(String sm4Key, String plain) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.ENCRYPT_MODE, sm4Key);
        return encodeUrlSafeBase64(cipher.doFinal(plain.getBytes("UTF-8")));
    }

    private static String decryptParam(String sm4Key, String encryptContent) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.DECRYPT_MODE, sm4Key);
        return new String(cipher.doFinal(decodeBase64(encryptContent)), "UTF-8");
    }

    private static InputStream decryptStream(String sm4Key, InputStream encryptStream) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.DECRYPT_MODE, sm4Key);
        return new CipherInputStream(encryptStream, cipher);
    }

    private static Cipher getInitializedCipher(int mode, String sm4Key) {
        try {
            byte[] key = decodeBase64(sm4Key);
            Cipher cipher = Cipher.getInstance(SM4_CBC_PKCS5PADDING, BouncyCastleProvider.PROVIDER_NAME);
            IvParameterSpec spec = new IvParameterSpec(SM4_IV);
            Key secretKey = new SecretKeySpec(key, "SM4");
            cipher.init(mode, secretKey, spec);
            return cipher;
        } catch (Throwable throwable) {
            throw new RuntimeException("error happened when initialize cipher", throwable);
        }
    }

    private static String encodeUrlSafeBase64(byte[] input) {
        return Base64.encodeBase64URLSafeString(input);
    }

    private static byte[] generateRandomKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        //实例化
        KeyGenerator generator = KeyGenerator.getInstance("SM4", BouncyCastleProvider.PROVIDER_NAME);
        //设置密钥长度，SM4算法目前只支持128位（即密钥16字节）
        generator.init(128);
        //生成密钥
        return generator.generateKey().getEncoded();
    }

    protected static HttpUriRequest buildHttpRequest(String requestUrl, YopRequestMethod requestMethod,
                                                     Map<String, String> headers, Multimap<String, String> params,
                                                     String content) throws UnsupportedEncodingException {
        RequestBuilder requestBuilder;
        if (YopRequestMethod.POST == requestMethod) {
            requestBuilder = RequestBuilder.post();
        } else if (YopRequestMethod.GET == requestMethod) {
            requestBuilder = RequestBuilder.get();
        } else {
            throw new RuntimeException("unsupported http method");
        }
        requestBuilder.setUri(requestUrl);

        // header
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }

        // body
        try {
            if (null != params) {
                for (Map.Entry<String, Collection<String>> entry : params.asMap().entrySet()) {
                    String paramKey = entry.getKey();
                    for (String value : entry.getValue()) {
                        requestBuilder.addParameter(paramKey, URLEncoder.encode(value, DEFAULT_ENCODING));
                    }
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("unable to create http request.", ex);
        }
        if (YopRequestMethod.GET.equals(requestMethod)) {
            requestBuilder.addHeader(CONTENT_TYPE, "application/x-www-form-urlencoded;charset=UTF-8");
            return requestBuilder.build();
        }
        // json 请求
        if (StringUtils.isNotBlank(content)) {
            final byte[] contentBytes = content.getBytes(DEFAULT_ENCODING);
            requestBuilder.setEntity(new InputStreamEntity(new ByteArrayInputStream(contentBytes), contentBytes.length));
            requestBuilder.addHeader(CONTENT_TYPE, "application/json;charset=UTF-8");
        }
        return requestBuilder.build();
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static void handleResponse(YopRequestType requestType,
                                       CloseableHttpResponse httpResponse,
                                       String encryptKey) throws Exception {
        // header
        Map<String, String> headers = Maps.newHashMap();
        for (Header header : httpResponse.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        String encryptHeader = headers.get("x-yop-encrypt"),// 加密头
                signHeader = headers.get("x-yop-sign"); //签名头

        boolean isEncryptResponse = StringUtils.isNotBlank(encryptHeader);

        // body
        HttpEntity entity = httpResponse.getEntity();
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode / 100 == HttpStatus.SC_OK / 100 && statusCode != HttpStatus.SC_NO_CONTENT) {
            //not a error
            if (null != entity && entity.getContent() != null) {
                if (isJsonResponse(httpResponse)) {
                    String content = IOUtils.toString(entity.getContent(), DEFAULT_ENCODING);
                    System.out.println("Request success, response:" + content);
                    final JsonNode bizData = OBJECT_MAPPER.readTree(content).get("result");
                    if (isEncryptResponse) {
                        System.out.println("Response decrypt success, bizData:" + decryptParam(encryptKey, bizData.asText()));
                    }
                    return;
                } else if (YopRequestType.FILE_DOWNLOAD.equals(requestType) || isDownloadResponse(httpResponse)) {
                    InputStream fileContent = entity.getContent();
                    if (isEncryptResponse) {
                        fileContent = decryptStream(encryptKey, fileContent);
                        System.out.println("Response file decrypt success");
                    }
                    final File file = saveFile(fileContent, headers);
                    System.out.println("Request success, file downloaded:" + file);
                    return;
                } else {
                    throw new RuntimeException("Response Error, contentType:" + httpResponse.getEntity().getContentType());
                }
            } else {
                throw new RuntimeException("Response Error, contentType:" + httpResponse.getEntity().getContentType());
            }
        } else if (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode != HttpStatus.SC_BAD_GATEWAY) {
            if (entity.getContent() != null) {
                String content = IOUtils.toString(entity.getContent(), DEFAULT_ENCODING);
                System.out.println("Request Fail, response:" + content);
                return;
            } else {
                throw new RuntimeException("ResponseError, Empty Content, httpStatusCode:" + httpResponse.getStatusLine().getStatusCode());
            }
        } else if (statusCode == HttpStatus.SC_BAD_GATEWAY || statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new RuntimeException("Response Error, statusCode:" + statusCode);
        }
        throw new RuntimeException("ReqParam Illegal, Bad Request, statusCode:" + statusCode);
    }

    /**
     * 保存文件到本地
     *
     * @param content
     * @param headers
     * @return File
     */
    public static File saveFile(InputStream content, Map<String, String> headers) {
        InputStream fileContent = content;
        try {
            String filePrefix = "yos-", fileSuffix = ".tmp";
            final String contentDisposition = headers.get(CONTENT_DISPOSITION);
            try {
                String fileName = getFileNameFromHeader(contentDisposition);
                if (StringUtils.isNotBlank(fileName)) {
                    final String[] split = fileName.split("\\.");
                    if (split.length == 2) {
                        if (StringUtils.length(split[0])  > 3) {
                            filePrefix = split[0];
                        }
                        if (StringUtils.isNotBlank(split[1])) {
                            fileSuffix = "." + split[1];
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(("parse Content-Disposition fail, value:" + contentDisposition + ", ex:" + e));
            }
            File tmpFile = File.createTempFile(filePrefix, fileSuffix);
            IOUtils.copy(fileContent, Files.newOutputStream(tmpFile.toPath()));
            return tmpFile;
        } catch (Throwable ex) {
            throw new RuntimeException("fail to save file");
        } finally {
            closeQuietly(fileContent);
        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileNameFromHeader(String contentDisposition) {
        if (StringUtils.isBlank(contentDisposition)) {
            return null;
        }

        final String[] parts = contentDisposition.split( "filename=");
        if (parts.length == 2) {
            return StringUtils.trim(parts[1]);
        }
        return null;
    }

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_STREAM = "application/octet-stream";

    private static boolean isJsonResponse(CloseableHttpResponse response) {
        return null != response.getEntity() && StringUtils.startsWith(response.getEntity().getContentType().getValue(), CONTENT_TYPE_JSON);
    }

    private static boolean isDownloadResponse(CloseableHttpResponse response) {
        return null != response.getEntity() && StringUtils.startsWith(response.getEntity().getContentType().getValue(), CONTENT_TYPE_STREAM);
    }

    public enum YopRequestMethod {
        GET,
        POST
    }

    public enum YopRequestType {
        WEB,
        FILE_DOWNLOAD,
        MULTI_FILE_UPLOAD,
    }

    private static final String YOP_HTTP_CONTENT_TYPE_JSON = "application/json";
    private static final String YOP_HTTP_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    private static final String YOP_HTTP_CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";
    private static final String YOP_HTTP_CONTENT_TYPE_STREAM = "application/octet-stream";
    private static final String YOP_HTTP_CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8";

    public enum YopRequestContentType {
        FORM_URL_ENCODE(YOP_HTTP_CONTENT_TYPE_FORM),
        MULTIPART_FORM(YOP_HTTP_CONTENT_TYPE_MULTIPART_FORM),
        JSON(YOP_HTTP_CONTENT_TYPE_JSON),
        OCTET_STREAM(YOP_HTTP_CONTENT_TYPE_STREAM),
        TEXT_PLAIN(YOP_HTTP_CONTENT_TYPE_TEXT);

        private String value;

        YopRequestContentType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
