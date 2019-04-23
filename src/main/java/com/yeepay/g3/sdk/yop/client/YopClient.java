package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.Multimap;
import com.yeepay.g3.sdk.yop.encrypt.AESEncrypter;
import com.yeepay.g3.sdk.yop.encrypt.Digests;
import com.yeepay.g3.sdk.yop.http.Headers;
import com.yeepay.g3.sdk.yop.http.HttpMethodName;
import com.yeepay.g3.sdk.yop.unmarshaller.JacksonJsonMarshaller;
import com.yeepay.g3.sdk.yop.utils.CheckUtils;
import com.yeepay.g3.sdk.yop.utils.DateUtils;
import com.yeepay.g3.sdk.yop.utils.JsonUtils;
import com.yeepay.g3.sdk.yop.utils.checksum.CRC64Utils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.zip.CheckedInputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <pre>
 * 对称加密 Client，简化用户发起请求及解析结果的处理
 * </pre>
 *
 * @author baitao.ji
 * @version 2.0
 */
public class YopClient extends AbstractClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopClient.class);

    /**
     * 发起post请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse post(String apiUri, YopRequest request) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(apiUri, request);

        HttpUriRequest httpPost = buildFormHttpRequest(request, contentUrl, HttpMethodName.POST);
        YopResponse response = fetchContentByApacheHttpClient(httpPost);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse get(String apiUri, YopRequest request) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(apiUri, request);

        HttpUriRequest httpGet = buildFormHttpRequest(request, contentUrl, HttpMethodName.GET);
        YopResponse response = fetchContentByApacheHttpClient(httpGet);
        handleResult(request, response);
        return response;
    }

    /**
     * 发起get请求，以YopResponse对象返回
     *
     * @param apiUri  目标地址
     * @param request 客户端请求对象
     * @return 响应对象
     */
    public static YopResponse upload(String apiUri, YopRequest request) throws IOException {
        CheckUtils.checkApiUri(apiUri);
        String contentUrl = richRequest(apiUri, request);
        normalize(request);
        sign(apiUri, request);

        Pair<HttpUriRequest, List<CheckedInputStream>> pair = buildMultiFormRequest(request, contentUrl);
        YopResponse response = fetchContentByApacheHttpClient(pair.getLeft());
        handleResult(request, response);
        if (pair.getRight() != null) {
            checkFileIntegrity(response, CRC64Utils.getCRC64(pair.getRight()));
        }
        return response;
    }

    /**
     * 适用于 AES 签名方式
     *
     * @param apiUri
     * @param request
     * @param forSignature
     * @return
     */
    public static String getCanonicalQueryString(String apiUri, YopRequest request, boolean forSignature) {
        request.addParam("ts", String.valueOf(System.currentTimeMillis()));

        Map<String, String> signParams = new TreeMap<String, String>();
        Multimap<String, String> parameters = request.getParams();
        if (!parameters.isEmpty()) {
            for (Map.Entry<String, Collection<String>> entry : parameters.asMap().entrySet()) {
                if (forSignature &&
                        (Headers.AUTHORIZATION.equalsIgnoreCase(entry.getKey()) || request.getIgnoreSignParams().contains(entry.getKey()))) {
                    continue;
                }
                String key = entry.getKey();
                checkNotNull(key, "parameter key should not be null");
                List<String> list = new ArrayList<String>(entry.getValue());
                Collections.sort(list);
                signParams.put(key, StringUtils.join(list, ","));
            }
        }

        signParams.put("method", apiUri);
        signParams.put("v", StringUtils.substringBefore(StringUtils.substringAfter(apiUri, "/v"), "/"));

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            sb.append(entry.getKey()).append(StringUtils.trim(entry.getValue()));
        }
        return sb.toString();
    }

    private static void normalize(YopRequest request) {
        request.addHeader(Headers.YOP_SESSION_ID, SESSION_ID);

        // 归一化
        if (!request.getHeaders().containsKey(Headers.YOP_REQUEST_ID)) {
            request.addHeader(Headers.YOP_REQUEST_ID, getUUID());
        }

        String timestamp = DateUtils.formatCompressedIso8601Timestamp(System.currentTimeMillis());
        request.addHeader(Headers.YOP_DATE, timestamp);
    }

    private static void sign(String apiUri, YopRequest request) {
        if (request.getHeaders().containsKey("Authorization")) {
            return;
        }
        String canonicalQueryString = getCanonicalQueryString(apiUri, request, true);

        String secret = request.getAesSecretKey();
        String algName = request.getSignAlg();
        algName = StringUtils.isBlank(algName) ? YopConstants.ALG_SHA1 : algName;

        String sb = secret + canonicalQueryString + secret;
        String signature = Digests.digest2Hex(sb, algName);
        if (32 == Base64.decodeBase64(secret).length) {
            request.addHeader("Authorization", "YOP-HMAC-AES256 " + signature);
        } else {
            request.addHeader("Authorization", "YOP-HMAC-AES128 " + signature);
        }
//        request.getParams().put("sign", signature);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("========\ncanonicalQueryString:" + canonicalQueryString
                    + "\nsha1(secret):" + Digests.digest2Hex(secret, "sha1")
                    + "\nsignature:" + signature);
        }
    }

    private static void handleResult(YopRequest request, YopResponse response) {
        String stringResult = response.getStringResult();
        if (StringUtils.isNotBlank(stringResult)) {
            response.setResult(JacksonJsonMarshaller.unmarshal(stringResult, Object.class));
        }
    }

    /**
     * //TODO 商户通知重新定义新二进制协议
     * -------------------------商户通知--------------------------------------------------
     */
    public static String acceptNotificationAsJson(String key, String response) {
        return validateAndDecryptNotification(key, response);
    }

    public static Map acceptNotificationAsMap(String key, String response) {
        String s = acceptNotificationAsJson(key, response);
        return s == null ? null : JsonUtils.fromJsonString(acceptNotificationAsJson(key, response), Map.class);
    }

    private static String validateAndDecryptNotification(String key, String response) {
        Map map = JsonUtils.fromJsonString(response, Map.class);
        boolean doEncryption = Boolean.valueOf(map.get("doEncryption").toString());
        String encryption = map.get("encryption").toString();
        String signature = map.get("signature").toString();
        String signatureAlg = map.get("signatureAlg").toString();

        //如果加密，解密
        if (doEncryption) {
            encryption = AESEncrypter.decrypt(encryption, key);
        }

        //签名是必须的...
        String localSignature = Digests.digest2Hex(key + encryption + key, signatureAlg);
        //验签失败...
        if (!localSignature.equals(signature)) {
            return null;
        }
        return encryption;
    }
}
