/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.example;

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
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: rsa 加密示例<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/9/27
 */
public class YopRsaEncryptExample {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final String appKey = "sandbox_rsa_10080041523";
//    private static final String appKey = "app_100800191870146";
//    private static final String isvPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCcl9jdReLN2vk57DWeu2l223gO6txH4ra/cOkM1rHYSwUqrvVYUM61qNL4AZNO3nlBMUkEtZ5RyvBGajJ0OZidenqcnXHKTBOD6GcVpBsS4qCU5hIHspI0khYBhikl1z74E3HHe9sIwVz+gEx2TkCwH9aH4jh68YeKk25+tHeb75JPMhdj0S+IzWyNHFOOTPD6VQ3vCq1RDqutL56gVBIt6p4+ZQ4GkTDRQoD7OKWDZOkzXz+JnaLqIN/hnZ/8ag+0WY60rgpjXBAX1ezZtHGp33L4sqnIZIzIp38e1Oyfe+Ab1cBQkaRaBqKYRXjq7NXG0sfeuoFxJONPQfjSsYcxAgMBAAECggEABAJAJ0QBfycer7ysD7z5AXWzYGBjVMTJTGPZ134Mjf63qmTRu5nP/OcORZKWwJNh88kM9z2mCK6DEa5ozcBmt4tZ5bYDInxpmHwj3XI2zjg2h7FPH1rTMtzViuLyHTmiL0wiIsr5K8N1e79xlarBrbCW96ITM5SI1YOaNcytbjS82Y0b6AS3Jx4H02ihcpzFRPYZNffLhE3lu4WN6aoiQUnWvC+agwF88osHmUIwrKlo9unwzjzOh2bXGSvzTESeGdDMEz4ekNRX8dEfC31sJwd2tUqha5hTev/oFgymNwFhQvj0OfnB1oKjf7gaRbOYjFf5P3nou5d4EBR7OyyTpQKBgQC6pXRqwpVh8Hhvf0/vFYZk5yQsKBi/ik4vyYtIZVtdZGIq4vjUpIftt9WoxTYKhjgOL0vsPvtbR79XnMFOb0PSZXARfMoUlAwtMM+mk9N3fb5a6jrgV617Ob1Oqf691WPqdprb6t1HMBSYTzD3J1mPKeGmItsTAlFZuUjzkyvN3QKBgQDWx52jTXvc4QyOqu4Fc0mPSDJEYBBB6XpnTXRIT8rUFD9SDTWRhb29UL2nrtoQ6/8ZTC+/wKQ5B9UAUEwNxSk8IsrWDhcuioxOc1rT7nPRoVXQarr35//NV1CPmZyn6ybpCqDTsy6mvtWLIJdjZCc+1yZevM1uvOtRMrWRhs8bZQKBgEqIpfu4JqVMvRtxUL9d7iQ/NW+4t2FN3rkwl8FaUGj0HEuaBdoMtgdVASp7ToBXZu0rL/twjzm9ZgibnYov3nqXbXBeT+h10oL9Wf7gS3MNMMXngYlzGeD6hsFyGzs9ir/niyHFIYY7Cg5kmV4pRZdpFyYcBzYJF+lnl11FaRm1AoGAFu1wMoKO+mE7ye8NQZ+w9o6qbwoiMicOXgCyrRV3fXQ73jJyyXoRayg3VrMfrDbFIJo1bq7N2Riw8DuiIsYtRLIiHP+cEefQWn+N7pnB21rxojICi3xEnlL30px/UJ2VpcLwsCisjjhI63UrM/z5A4hMHEjjVTLtm9lh8IsHiNECgYBf+YEmhG88617fUMa7p9VLcwZNXWOgAFX230CYBFQQIgDuwINASoM4GGVfnvLPym4+WNW3cvfI1sNSm3VXzoM+uSHZtQUkG0SjHZl3buNGXXaJ1iZK/EoRHGraukC6ZuaDsnnjFQbRqvdZIFXqEWFbPFSWBQm9g2dYSbzNu3u9Kg==";
    private static final String isvPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDChomOdGrRa/D889B9ARtsLb/9zVJxq7VPV9yTu6ZjbhtpwS8Qep/95W7pWcK9yvNH5pWPpYZYwRB55z2Z/9SVqn+2sVU18JCxsgw7o35FUg9Qu9eiZeRpCiV8fbjhI9ZJpDh4v48MKcYBRM0LBZSQjA67xh4K90PYxI7UAgnMWW25Ny37oYxmH8ZoihemWvaRPFx4k4c9knBL5aPEspwIQjfDztOcOoRMkhZo5hUuI/GsKlw0REZ5lUdGKzgQ5ec4ZBe4ijFTsEjSvHcLaJbVnw9PdQ7a80Gw0Cf3qwS6dOR9LgsPLoUsKf9XRu0X6csS5uqu4e4I8lJm+WNZb+l3AgMBAAECggEAFeSOCwjXfsiptND8mB0C85VgjsAddRVF+281hYZF2dnqCZMdk2Vhp/G686tPq2Gcfhfu9t2Xta8g22oRjklIfoGpDFbVSBP84kAvd+9/cMN6ssjj928v75HIme63sIBX3S43fCt+/iJIxRrJAuJhZTVGG+RWZus2Pmlnc704/L+qP93XOVwFk/hKXhy7/Aa2S7KjVr5SpEDNUJ94W8WEpFgfccCrlkbuLAWG9nJF1gAoi0w6AJfEsTWpnNjpdHfFDtcT9UdULgJuz9yhzZya+mYbcjmLATqFihXJWsw4AKPnjOyMRPZP3EdtmOdiRddRnj82dsN5pjz651xFk5EuvQKBgQDWScg70XXwXgm527Kytra81VHW/WKcOw1w37GUiLJG4324fJeupXJ2zt4bmILrLhPTJkDNxE/1roX7GR1tn523H9s29b9Dqwfr38MSjfvJ5yPlaXrOLzDD7myDhZHTqvqgV1/id0IJZ3BeXTVEwd8yjfwEuZHW5s8TilOLoIcXswKBgQDoY/WTLfNleJLHFG4iIDbYBdK7zx105WVOmZF893sEP/4XPVq4RakSZG3CpVpYH5bsXyPTjwUoCoLLXmJzSJFLISemkn4Ot8xJRWPgZoDqm1Fi72O6VgNJlu9lX2ZcMKH4pAXqe7WMBSrxZqXL52gZsyv5YM+uBkY29DtHp7zFLQKBgDn1juEPEHVJGhxZHgZUgSymDhK2SjuzhTkoZ+Gi74VY9qI1oNkuCr2zykNwhsiRl+8eg5ykInRzFe4KpvkFmST0ytgcs/Tbh7L2vM6B9L5xdDYSx5KJFQmJrXQNZpn3vv4rY9XfJ89fWPdNAqFsRrBn0uh8QMP9fbjtSxeS/bcdAoGBAJTJqymYegW1tQQRaJIg3fxhfhMRAGMfnEU+vY+tQ+3sqtpmRfdFYoKMGlpNVBKn5xFfuKhzIXIJiMR8obv98kiP6bsUf/EcbIddDh1Wg6Ox3eHiM4/SEjjDknLtKbRMzudK3R7MJeiIRn5Yoj5y4ovR043PFijti3cT2ACAvLPhAoGAX5zTGz4M/JuqF5TWLKARGAZ9HiYoTLtJs5l/yTkKR4NO/E79TsBlUfDBhVYb+A3ChN6P8JX3cSwUplSiUo12eqG9/DPUSmeRrhTXlbul7metzaEVl1fQhaOIHflxkvg+FZIIt+NHyi6oGmDPXYfwUU3QqifMP5mF+v7IjTcPyD4=";
    private static final String yopPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4g7dPL+CBeuzFmARI2GFjZpKODUROaMG+E6wdNfv5lhPqC3jjTIeljWU8AiruZLGRhl92QWcTjb3XonjaV6k9rf9adQtyv2FLS7bl2Vz2WgjJ0FJ5/qMaoXaT+oAgWFk2GypyvoIZsscsGpUStm6BxpWZpbPrGJR0N95un/130cQI9VCmfvgkkCaXt7TU1BbiYzkc8MDpLScGm/GUCB2wB5PclvOxvf5BR/zNVYywTEFmw2Jo0hIPPSWB5Yyf2mx950Fx8da56co/FxLdMwkDOO51Qg3fbaExQDVzTm8Odi++wVJEP1y34tlmpwFUVbAKIEbyyELmi/2S6GG0j9vNwIDAQAB";

    private static final String serverRoot = "https://sandbox.yeepay.com/yop-center";
    private static final String apiUri = "/rest/v1.0/test/old-api-mgr/find-api-by-uri";
    private static final String jsonApiUri = "/rest/v1.0/test-wdc/test/http-json/test";
    private static final String SLASH = "/";
    private static final String UNDER_LINE = "_";
    private static final String SEMICOLON = ";";
    private static final String EMPTY = "";

    private static final String aesEncryptAlg = "AES/ECB/PKCS5Padding";
    private static final String rsaEncryptAlg = "RSA/ECB/PKCS1Padding";
    private static final String STREAM = "stream";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String DEFAULT_YOP_PROTOCOL_VERSION = "yop-auth-v3";
    public static final String DEFAULT_AUTH_PREFIX_RSA2048 = "YOP-RSA2048-SHA256";
    private static final String RSA = "RSA";
    private static final String DEFAULT_DIGEST_ALG = "SHA-256";
    private static final String DEFAULT_SIGN_ALG = "SHA256withRSA";
    private static final Joiner QUERY_STRING_JOINER = Joiner.on('&');

    public static void main(String[] args) throws Exception {
        postFormExample();
        postJsonExample();
        // TODO getFormExample
    }

    private static void postFormExample() throws Exception {
        final HttpMethodName post = HttpMethodName.POST;
        // 请求加密
        String aesKey = encodeUrlSafeBase64(generateRandomKey());
        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Sets.newHashSet();
        String paramKey = "apiUri";
        String paramPlainValue = "/rest/v1.0/test/old-api-mgr/find-api-by-uri",
                paramEncryptValue = encryptParam(aesKey, paramPlainValue);
        Multimap<String, String> params = ArrayListMultimap.create();
        // 不加密
//        params.put(paramKey, paramPlainValue);

        // 加密
        encryptParams.add(paramKey);
        params.put(paramKey, paramEncryptValue);

        // 请求头
        Map<String, String> headers = new HashMap<>();
        headers.put(YOP_SDK_LANGS, "java");
        headers.put(YOP_SDK_VERSION, "4.3.0");
        headers.put(YOP_APPKEY, appKey);
        headers.put(YOP_REQUEST_ID, UUID.randomUUID().toString());

        // 加密头：请求参数不加密的情况, 也需设置加密头，用于响应结果加密
        headers.put(YOP_ENCRYPT,buildEncryptHeader(encryptHeaders, encryptParams, aesKey));

        // 摘要头
        headers.put(YOP_CONTENT_SHA256, calculateContentHash(params));

        // 签名头
        headers.put(AUTHORIZATION, signRequest(post, apiUri, headers, params));

        // 构造请求
        HttpUriRequest request = buildHttpRequest(serverRoot + apiUri,
                post, headers, params, null);

        // 发起请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            handleResponse(YopRequestType.WEB, response);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
    }

    private static void postJsonExample() throws Exception {
        final HttpMethodName post = HttpMethodName.POST;
        // 请求加密
        String aesKey = encodeUrlSafeBase64(generateRandomKey());
        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Sets.newHashSet();

        String plainJsonContent = ("{\n" +
                "  \"arg1\" : {\n" +
                "    \"appId\" : \"app_1111111111\",\n" +
                "    \"customerNo\" : \"333333333\"\n" +
                "  },\n" +
                "  \"arg0\" : {\n" +
                "    \"string\" : \"hello\",\n" +
                "    \"array\" : [ \"test\" ]\n" +
                "  }\n" +
                "}"),
                encryptJsonContent = encryptParam(aesKey, plainJsonContent);
        String finalJsonContent
        // 不加密
//                = plainJsonContent;
        // 加密
              = encryptJsonContent;
        encryptParams.add("$");

        // 请求头
        Map<String, String> headers = new HashMap<>();
        headers.put(YOP_SDK_LANGS, "java");
        headers.put(YOP_SDK_VERSION, "4.3.0");
        headers.put(YOP_APPKEY, appKey);
        headers.put(YOP_REQUEST_ID, UUID.randomUUID().toString());

        // 加密头：请求参数不加密的情况, 也需设置加密头，用于响应结果加密
        headers.put(YOP_ENCRYPT,buildEncryptHeader(encryptHeaders, encryptParams, aesKey));

        // 摘要头
        headers.put(YOP_CONTENT_SHA256, calculateContentHash(finalJsonContent));

        // 签名头
        // 一般json接口不会再有form参数, 留空即可
        final ArrayListMultimap<String, String> params = ArrayListMultimap.create();
        headers.put(AUTHORIZATION, signRequest(post, jsonApiUri, headers, params));

        // 构造请求
        HttpUriRequest request = buildHttpRequest(serverRoot + jsonApiUri,
                post, headers, params, finalJsonContent);

        // 发起请求
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            handleResponse(YopRequestType.WEB, response);
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
    private static final String YOP_CONTENT_SHA256 = "x-yop-content-sha256";
    private static final String YOP_ENCRYPT = "x-yop-encrypt";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_TYPE = "Content-Type";

    static {
        DEFAULT_HEADERS_TO_SIGN.add(YOP_REQUEST_ID);
        DEFAULT_HEADERS_TO_SIGN.add(YOP_APPKEY);
        DEFAULT_HEADERS_TO_SIGN.add(YOP_CONTENT_SHA256);
        DEFAULT_HEADERS_TO_SIGN.add(YOP_ENCRYPT);
    }

    private static String signRequest(HttpMethodName httpMethod, String apiUri, Map<String, String> headers, Multimap<String, String> params) throws Exception {
        // A.构造认证字符串
        String authString = buildAuthString();

        // B.获取规范请求串
        SortedMap<String, String> headersToSign = getHeadersToSign(headers, DEFAULT_HEADERS_TO_SIGN);
        String canonicalRequest = buildCanonicalRequest(httpMethod, apiUri, authString, headersToSign);

        // C.计算签名
        String signature = encodeUrlSafeBase64(sign(canonicalRequest.getBytes(DEFAULT_ENCODING))) + "$" + "SHA256";

        // D.添加认证头
        return buildAuthHeader(authString, headersToSign, signature);
    }

    private static String buildAuthHeader(String authString,
                                          SortedMap<String, String> headersToSign,
                                          String signature) {
        String signedHeaders = SIGNED_HEADER_STRING_JOINER.join(headersToSign.keySet());
        signedHeaders = signedHeaders.trim().toLowerCase();
        return DEFAULT_AUTH_PREFIX_RSA2048 + " " + authString + "/" + signedHeaders + "/" + signature;
    }

    private static byte[] sign(byte[] data) {
        try {
            Signature signature = Signature.getInstance(DEFAULT_SIGN_ALG);
            signature.initSign(string2PrivateKey(isvPrivateKey));
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Sign Fail, key:" + isvPrivateKey + ", ex:", e);
        }
    }

    private static PrivateKey string2PrivateKey(String priKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePrivate(
                    new PKCS8EncodedKeySpec(decodeBase64(priKey)));
        } catch (Exception e) {
            throw new RuntimeException("ConfigProblem, IsvPrivateKey ParseFail, value:" + priKey + ", ex:", e);
        }
    }

    private static byte[] decodeBase64(String input) {
        return Base64.decodeBase64(input);
    }

    private static String buildCanonicalRequest(HttpMethodName httpMethod, String apiUri, String authString,
                                                SortedMap<String, String> headersToSign) {
        String canonicalQueryString = "";// post from 与json时，均为空，此处先简单处理
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
                + appKey + "/"
                + alternateIso8601DateFormat.print(new DateTime(timestamp)) + "/"
                + "1800";
    }

    private static String calculateContentHash(Multimap<String, String> params) throws Exception {
        InputStream contentStream = getContentStream(params);
        return Hex.encodeHexString((digest(contentStream)));
    }

    private static String calculateContentHash(String jsonContent) throws Exception {
        InputStream contentStream = getContentStream(jsonContent);
        return Hex.encodeHexString((digest(contentStream)));
    }

    private static ByteArrayInputStream getContentStream(Multimap<String, String> params) throws Exception {
        return getContentStream(getCanonicalQueryString(params.asMap(), true));
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

    public static String getCanonicalQueryString(Map<String, Collection<String>> parameters, boolean forSignature) {
        if (parameters.isEmpty()) {
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
            MessageDigest md = MessageDigest.getInstance(DEFAULT_DIGEST_ALG);
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
                                            String aesKey) throws Exception {

        return  "yop-encrypt-v1" + SLASH +
                EMPTY + SLASH + //rsa 为空
                StringUtils.replace(aesEncryptAlg, SLASH, UNDER_LINE) + SLASH +
                encodeUrlSafeBase64(encryptKey(decodeBase64(aesKey), string2PublicKey(yopPublicKey))) + SLASH +
                EMPTY + SLASH +
                STREAM + SLASH +
                StringUtils.join(encryptHeaders, SEMICOLON) + SLASH +
                encodeUrlSafeBase64(StringUtils.join(encryptParams, SEMICOLON).getBytes(DEFAULT_ENCODING));
    }

    public static PublicKey string2PublicKey(String pubKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePublic(
                    new X509EncodedKeySpec(decodeBase64(pubKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("ConfigProblem, YopPublicKey ParseFail, value:" + pubKey + ", ex:", e);
        }
    }

    private static byte[] encryptKey(byte[] data, Key key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(rsaEncryptAlg);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Encrypt Fail, key:" + key + "ex:", e);
        }
    }

    private static String encryptParam(String aesKey, String plain) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.ENCRYPT_MODE, aesKey);
        return encodeUrlSafeBase64(cipher.doFinal(plain.getBytes("UTF-8")));
    }

    private static Cipher getInitializedCipher(int mode, String aesKey) {
        try {
            byte[] key = decodeBase64(aesKey);
            Cipher cipher = Cipher.getInstance(aesEncryptAlg);
            Key secretKey = new SecretKeySpec(key, "AES");
            cipher.init(mode, secretKey);
            return cipher;
        } catch (Throwable throwable) {
            throw new RuntimeException("error happened when initialize cipher", throwable);
        }
    }

    private static String encodeUrlSafeBase64(byte[] input) {
        return Base64.encodeBase64URLSafeString(input);
    }

    private static byte[] generateRandomKey() throws NoSuchAlgorithmException {
        //实例化
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        //设置密钥长度
        generator.init(128);
        //生成密钥
        return generator.generateKey().getEncoded();
    }

    protected static HttpUriRequest buildHttpRequest(String requestUrl, HttpMethodName requestMethod,
                                                     Map<String, String> headers, Multimap<String, String> params,
                                                     String entityString) throws UnsupportedEncodingException {
        RequestBuilder requestBuilder;
        if (HttpMethodName.POST == requestMethod) {
            requestBuilder = RequestBuilder.post();
        } else if (HttpMethodName.GET == requestMethod) {
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
        // json 请求
        if (StringUtils.isNotBlank(entityString)) {
            final byte[] contentBytes = entityString.getBytes(DEFAULT_ENCODING);
            requestBuilder.setEntity(new InputStreamEntity(new ByteArrayInputStream(contentBytes), contentBytes.length));
            requestBuilder.addHeader(CONTENT_TYPE, "application/json;charset=UTF-8");
        }
        return requestBuilder.build();
    }

    private static void handleResponse(YopRequestType requestType,
                                       CloseableHttpResponse httpResponse) throws IOException {
        // header
        Map<String, String> headers = Maps.newHashMap();
        for (Header header : httpResponse.getAllHeaders()) {
            headers.put(header.getName(), header.getValue());
        }
        String encryptHeader = headers.get("x-yop-encrypt"),// 加密头
                signHeader = headers.get("x-yop-sign"); //签名头

        // body
        HttpEntity entity = httpResponse.getEntity();
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode / 100 == HttpStatus.SC_OK / 100 && statusCode != HttpStatus.SC_NO_CONTENT) {
            //not a error
            if (null != entity && entity.getContent() != null) {
                if (isJsonResponse(httpResponse)) {
                    String content = IOUtils.toString(entity.getContent(), DEFAULT_ENCODING);
                    // TODO 解密
                    System.out.println("Request success, response:" + content);
                    return;
                } else if (YopRequestType.FILE_DOWNLOAD.equals(requestType) || isDownloadResponse(httpResponse)) {
                    final File file = saveFile(entity.getContent(), headers);
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
//                verifySignature(content, response.getHeader(Headers.YOP_SIGN), responseConfig.getYopPublicKey());
//                content = decryptResponse(content, responseConfig);

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

    public enum HttpMethodName {
        GET,
        POST
    }

    public enum YopRequestType {
        WEB,
        FILE_DOWNLOAD,
        MULTI_FILE_UPLOAD,
    }
}
