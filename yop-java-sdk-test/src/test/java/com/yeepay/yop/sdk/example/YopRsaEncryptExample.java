/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopRsaEncryptExample.class);

    //须引入第三方包 org.apache.httpcomponents:httpclient
    private static final CloseableHttpClient httpClient;
    //须引入第三方包 com.fasterxml.jackson.core:jackson-databind
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        // 自定义连接池大小、默认超时时间
        httpClient = HttpClients.custom()
                .setMaxConnPerRoute(100)
                .setMaxConnTotal(200)
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectionRequestTimeout(3000)
                                .setConnectTimeout(3000)
                                .setSocketTimeout(30000)
                                .build())
                .build();
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        OBJECT_MAPPER.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        // 小数都用BigDecimal，默认的是Double
        OBJECT_MAPPER.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        OBJECT_MAPPER.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
    }

    private static final String APP_KEY = "sandbox_rsa_10080041523";
    private static final String ISV_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDChomOdGrRa/D889B9ARtsLb/9zVJxq7VPV9yTu6ZjbhtpwS8Qep/95W7pWcK9yvNH5pWPpYZYwRB55z2Z/9SVqn+2sVU18JCxsgw7o35FUg9Qu9eiZeRpCiV8fbjhI9ZJpDh4v48MKcYBRM0LBZSQjA67xh4K90PYxI7UAgnMWW25Ny37oYxmH8ZoihemWvaRPFx4k4c9knBL5aPEspwIQjfDztOcOoRMkhZo5hUuI/GsKlw0REZ5lUdGKzgQ5ec4ZBe4ijFTsEjSvHcLaJbVnw9PdQ7a80Gw0Cf3qwS6dOR9LgsPLoUsKf9XRu0X6csS5uqu4e4I8lJm+WNZb+l3AgMBAAECggEAFeSOCwjXfsiptND8mB0C85VgjsAddRVF+281hYZF2dnqCZMdk2Vhp/G686tPq2Gcfhfu9t2Xta8g22oRjklIfoGpDFbVSBP84kAvd+9/cMN6ssjj928v75HIme63sIBX3S43fCt+/iJIxRrJAuJhZTVGG+RWZus2Pmlnc704/L+qP93XOVwFk/hKXhy7/Aa2S7KjVr5SpEDNUJ94W8WEpFgfccCrlkbuLAWG9nJF1gAoi0w6AJfEsTWpnNjpdHfFDtcT9UdULgJuz9yhzZya+mYbcjmLATqFihXJWsw4AKPnjOyMRPZP3EdtmOdiRddRnj82dsN5pjz651xFk5EuvQKBgQDWScg70XXwXgm527Kytra81VHW/WKcOw1w37GUiLJG4324fJeupXJ2zt4bmILrLhPTJkDNxE/1roX7GR1tn523H9s29b9Dqwfr38MSjfvJ5yPlaXrOLzDD7myDhZHTqvqgV1/id0IJZ3BeXTVEwd8yjfwEuZHW5s8TilOLoIcXswKBgQDoY/WTLfNleJLHFG4iIDbYBdK7zx105WVOmZF893sEP/4XPVq4RakSZG3CpVpYH5bsXyPTjwUoCoLLXmJzSJFLISemkn4Ot8xJRWPgZoDqm1Fi72O6VgNJlu9lX2ZcMKH4pAXqe7WMBSrxZqXL52gZsyv5YM+uBkY29DtHp7zFLQKBgDn1juEPEHVJGhxZHgZUgSymDhK2SjuzhTkoZ+Gi74VY9qI1oNkuCr2zykNwhsiRl+8eg5ykInRzFe4KpvkFmST0ytgcs/Tbh7L2vM6B9L5xdDYSx5KJFQmJrXQNZpn3vv4rY9XfJ89fWPdNAqFsRrBn0uh8QMP9fbjtSxeS/bcdAoGBAJTJqymYegW1tQQRaJIg3fxhfhMRAGMfnEU+vY+tQ+3sqtpmRfdFYoKMGlpNVBKn5xFfuKhzIXIJiMR8obv98kiP6bsUf/EcbIddDh1Wg6Ox3eHiM4/SEjjDknLtKbRMzudK3R7MJeiIRn5Yoj5y4ovR043PFijti3cT2ACAvLPhAoGAX5zTGz4M/JuqF5TWLKARGAZ9HiYoTLtJs5l/yTkKR4NO/E79TsBlUfDBhVYb+A3ChN6P8JX3cSwUplSiUo12eqG9/DPUSmeRrhTXlbul7metzaEVl1fQhaOIHflxkvg+FZIIt+NHyi6oGmDPXYfwUU3QqifMP5mF+v7IjTcPyD4=";
    private static final String YOP_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4g7dPL+CBeuzFmARI2GFjZpKODUROaMG+E6wdNfv5lhPqC3jjTIeljWU8AiruZLGRhl92QWcTjb3XonjaV6k9rf9adQtyv2FLS7bl2Vz2WgjJ0FJ5/qMaoXaT+oAgWFk2GypyvoIZsscsGpUStm6BxpWZpbPrGJR0N95un/130cQI9VCmfvgkkCaXt7TU1BbiYzkc8MDpLScGm/GUCB2wB5PclvOxvf5BR/zNVYywTEFmw2Jo0hIPPSWB5Yyf2mx950Fx8da56co/FxLdMwkDOO51Qg3fbaExQDVzTm8Odi++wVJEP1y34tlmpwFUVbAKIEbyyELmi/2S6GG0j9vNwIDAQAB";

    private static final String SERVER_ROOT = "https://sandbox.yeepay.com/yop-center";
    private static final String SLASH = "/";
    private static final String UNDER_LINE = "_";
    private static final String SEMICOLON = ";";
    private static final String EMPTY = "";

    private static final String AES_ENCRYPT_ALG = "AES/ECB/PKCS5Padding";
    private static final String RSA_ENCRYPT_ALG = "RSA/ECB/PKCS1Padding";
    private static final String STREAM = "stream";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String DEFAULT_YOP_PROTOCOL_VERSION = "yop-auth-v3";
    public static final String DEFAULT_AUTH_PREFIX_RSA2048 = "YOP-RSA2048-SHA256";
    private static final String RSA = "RSA";
    private static final String DEFAULT_DIGEST_ALG = "SHA-256";
    private static final String DEFAULT_SIGN_ALG = "SHA256withRSA";

    private static final String YOP_SDK_VERSION = "x-yop-sdk-version";
    private static final String YOP_SDK_LANGS = "x-yop-sdk-langs";
    private static final String YOP_REQUEST_ID = "x-yop-request-id";
    private static final String YOP_APPKEY = "x-yop-appkey";
    private static final String YOP_CONTENT_SHA256 = "x-yop-content-sha256";
    private static final String YOP_ENCRYPT = "x-yop-encrypt";
    private static final String YOP_SIGN = "x-yop-sign";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_TYPE = "Content-Type";

    public static void main(String[] args) throws Exception {
        // get请求，form参数，返回json
        getFormExample();

        // 文件下载接口，返回文件流
        downloadExample();

        // post请求，form参数，返回json
        postFormExample();

        // post请求，json参数，返回json
        postJsonExample();

        // post请求，文件上传，返回json
        uploadExample();
    }

    /**
     * HTTP GET 请求
     *
     * @param serverRoot 请求端点地址
     * @param apiUri 请求接口地址
     * @param params 查询参数Map
     * @param encryptParams 指定待加密参数名
     * @param yopPublicKey 平台公钥
     * @param isvPrivateKey 商户私钥
     * @return ApiResponse
     * @throws Exception
     */
    public static ApiResponse get(String serverRoot,
                                  String apiUri,
                                  Map<String, Object> params,
                                  Set<String> encryptParams,
                                  String yopPublicKey,
                                  String isvPrivateKey) throws Exception {
        // 请求类型，具体查看枚举注释
        YopRequestType requestType = YopRequestType.WEB;

        // 请求方法
        YopRequestMethod httpMethod = YopRequestMethod.GET;

        // 请求内容类型
        YopRequestContentType contentType = YopRequestContentType.FORM_URL_ENCODE;

        // GET 请求不支持http-body参数，留空即可
        Map<String, Object> bodyParams = null;

        return request(serverRoot, requestType, httpMethod, apiUri, contentType, params, bodyParams,
                encryptParams, yopPublicKey, isvPrivateKey);
    }

    /**
     * HTTP POST 请求
     *
     * @param serverRoot 请求端点地址
     * @param apiUri 请求接口地址
     * @param params 业务参数Map
     * @param contentType 内容类型(form/json)
     * @param encryptParams 指定待加密参数名
     * @param yopPublicKey 平台公钥
     * @param isvPrivateKey 商户私钥
     * @return ApiResponse
     * @throws Exception
     */
    public static ApiResponse post(String serverRoot,
                                  String apiUri, YopRequestContentType contentType,
                                  Map<String, Object> params,
                                  Set<String> encryptParams,
                                  String yopPublicKey,
                                  String isvPrivateKey) throws Exception {
        if (null == params || params.isEmpty()) {
            throw new IllegalArgumentException("biz params must not be null or empty");
        }

        // 纠正内容类型，具体查看枚举注释
        for (Object value : params.values()) {
            if (isFileParam(value)) {
                return upload(serverRoot, apiUri, params, encryptParams, yopPublicKey, isvPrivateKey);
            }
        }

        // 请求类型
        YopRequestType requestType = YopRequestType.WEB;

        // 请求方法
        YopRequestMethod httpMethod = YopRequestMethod.POST;

        // http-body参数，留空即可
        Map<String, Object> bodyParams = params;

        return request(serverRoot, requestType, httpMethod, apiUri, contentType, null, bodyParams,
                encryptParams, yopPublicKey, isvPrivateKey);
    }

    /**
     * HTTP POST 请求
     *
     * @param serverRoot 请求端点地址
     * @param apiUri 请求接口地址
     * @param params 业务参数Map
     * @param encryptParams 指定待加密参数名
     * @param yopPublicKey 平台公钥
     * @param isvPrivateKey 商户私钥
     * @return ApiResponse
     * @throws Exception
     */
    public static ApiResponse upload(String serverRoot,
                                  String apiUri,
                                  Map<String, Object> params,
                                  Set<String> encryptParams,
                                  String yopPublicKey,
                                  String isvPrivateKey) throws Exception {
        if (null == params || params.isEmpty()) {
            throw new IllegalArgumentException("biz params must not be null or empty");
        }

        YopRequestContentType contentType = YopRequestContentType.MULTIPART_FORM;

        // 请求类型
        YopRequestType requestType = YopRequestType.WEB;

        // 请求方法
        YopRequestMethod httpMethod = YopRequestMethod.POST;

        // http-body参数，留空即可
        Map<String, Object> bodyParams = params;

        return request(serverRoot, requestType, httpMethod, apiUri, contentType, null, bodyParams,
                encryptParams, yopPublicKey, isvPrivateKey);
    }

    /**
     * HTTP 文件下载 请求
     *
     * @param serverRoot 请求端点地址
     * @param apiUri 请求接口地址
     * @param httpMethod 请求方法
     * @param contentType 内容类型
     * @param params 参数Map
     * @param encryptParams 指定待加密参数名
     * @param yopPublicKey 平台公钥
     * @param isvPrivateKey 商户私钥
     * @return
     * @throws Exception
     */
    public static ApiResponse download(String serverRoot,
                                  String apiUri,
                                  YopRequestMethod httpMethod,
                                  YopRequestContentType contentType,
                                  Map<String, Object> params,
                                  Set<String> encryptParams,
                                  String yopPublicKey,
                                  String isvPrivateKey) throws Exception {
        // 请求类型，具体查看枚举注释
        YopRequestType requestType = YopRequestType.FILE_DOWNLOAD;

        // 请求不支持http-body参数，留空即可
        Map<String, Object> queryParams = null, bodyParams = null;

        if (YopRequestMethod.GET == httpMethod) {
            queryParams = params;
        } else {
            bodyParams = params;
        }

        return request(serverRoot, requestType, httpMethod, apiUri, contentType, queryParams, bodyParams,
                encryptParams, yopPublicKey, isvPrivateKey);
    }

    private static void downloadExample() throws Exception {
        // 接口地址
        String apiUri = "/rest/v1.0/test-wdc/test-param-parse/input-stream-result";

        // TODO 请求方法，根据接口文档，实际情况来选择GET/POST
        YopRequestMethod httpMethod = YopRequestMethod.GET;

        // TODO 请求内容类型，根据接口文档，实际情况来选择form/json
        YopRequestContentType contentType = YopRequestContentType.FORM_URL_ENCODE;

        //  TODO 构建业务参数，根据接口文档填充接口参数
        Map<String, Object> params = new HashMap<>();
        params.put("strParam", "xxx");

        // TODO 根据接口需要选择示例1或示例2，如果不涉及敏数据，建议走示例1
        // 示例1、参数不加密
        ApiResponse apiResponse1 = download(SERVER_ROOT, apiUri, httpMethod, contentType,
                params, null, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse1);

        // 示例2、参数加密，填充敏感字段名
        Set<String> encryptParams = new HashSet<>();
        encryptParams.add("strParam");
        ApiResponse apiResponse2 = download(SERVER_ROOT, apiUri, httpMethod, contentType,
                params, encryptParams, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse2);
    }

    private static void getFormExample() throws Exception {
        // 接口地址
        String apiUri = "/rest/v1.0/test/errorcode2";

        //  TODO 构建业务参数，根据接口文档填充接口参数
        Map<String, Object> params = new HashMap<>();
        params.put("errorCode", "000027");

        // TODO 根据接口需要选择示例1或示例2，如果不涉及敏数据，建议走示例1
        // 示例1、参数不加密
        ApiResponse apiResponse1 = get(SERVER_ROOT, apiUri, params, null, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse1);

        // 示例2、参数加密，填充敏感字段名
        Set<String> encryptParams = new HashSet<>();
        encryptParams.add("errorCode");
        ApiResponse apiResponse2 = get(SERVER_ROOT, apiUri, params, encryptParams, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse2);
    }

    private static void postFormExample() throws Exception {
        // 接口地址
        String apiUri = "/rest/v1.0/frontcashier/agreement/sign/confirm";

        // 请求内容类型
        YopRequestContentType contentType = YopRequestContentType.FORM_URL_ENCODE;

        // POST 请求http-body参数
        Map<String, Object> params = new HashMap<>();
        params.put("parentMerchant", "10080041523");
        params.put("merchantNo", "10080041523");
        params.put("smsCode", "11111131");
        params.put("merchantFlowId", "111111");

        // TODO 根据接口需要选择示例1或示例2，如果不涉及敏数据，建议走示例1
        // 示例1、参数不加密
        ApiResponse apiResponse1 = post(SERVER_ROOT, apiUri, contentType,
                params, null, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse1);

        // 示例2、参数加密，填充敏感字段名
        Set<String> encryptParams = new HashSet<>();
        encryptParams.add("smsCode");
        ApiResponse apiResponse2 = post(SERVER_ROOT, apiUri, contentType,
                params, encryptParams, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse2);
    }

    private static void uploadExample() throws Exception {
        // 接口地址
        String apiUri = "/yos/v1.0/sys/merchant/qual/upload";

        // POST 请求http-body参数
        Map<String, Object> params = new HashMap<>();
        params.put("clientId", APP_KEY);
        params.put("merQual", new FileParam(new URL("https://open.yeepay.com/apis/docs/apis/common/ALL.json").openStream(), "abc.json"));

        // TODO 根据接口需要选择示例1或示例2，如果不涉及敏数据，建议走示例1
        // 示例1、参数不加密
        ApiResponse apiResponse1 = upload(SERVER_ROOT, apiUri,
                params, null, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse1);

        // 示例2、参数加密，填充敏感字段名
//        Set<String> encryptParams = new HashSet<>();
//        encryptParams.add("clientId");
//        ApiResponse apiResponse2 = upload(SERVER_ROOT, apiUri,
//                params, encryptParams, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
//        LOGGER.info("apiResponse:{}", apiResponse2);
    }

    private static void postJsonExample() throws Exception {
        // 接口地址
        String apiUri = "/rest/v1.0/test-wdc/test/http-json/test";

        // 请求内容类型
        YopRequestContentType contentType = YopRequestContentType.JSON;

        // POST-json请求支持http-body参数，此处为嵌套复杂对象示例
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> arg0 = new HashMap<>();
        arg0.put("string","hello world");
        arg0.put("array", Arrays.asList("test"));
        params.put("arg0", arg0);
        Map<String, Object> arg1 = new HashMap<>();
        arg1.put("appId","app_1111111111");
        arg1.put("customerNo","333333333");
        params.put("arg1", arg1);

        // TODO 根据接口需要选择示例1或示例2，如果不涉及敏数据，建议走示例1
        // 示例1、参数不加密
        ApiResponse apiResponse1 = post(SERVER_ROOT, apiUri, contentType,
                params, null, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse1);

        // 示例2、参数加密，填充敏感字段名，json方式时，直接加密整个请求体即可
        Set<String> encryptParams = new HashSet<>();
        encryptParams.add("$");// $代表整体加密(所有参数均加密)
        ApiResponse apiResponse2 = post(SERVER_ROOT, apiUri, contentType,
                params, encryptParams, YOP_PUBLIC_KEY, ISV_PRIVATE_KEY);
        LOGGER.info("apiResponse:{}", apiResponse2);
    }

    /**
     * 请求响应处理
     *
     * @param serverRoot 请求端点地址
     * @param requestType 请求类型
     * @param httpMethod 请求方法
     * @param apiUri 请求接口地址
     * @param contentType 请求内容类型
     * @param queryParams http-query参数
     * @param bodyParams http-body参数
     * @param encryptParams 待加密参数名
     * @param yopPublicKey 平台公钥
     * @param isvPrivateKey 商户私钥
     * @return
     * @throws Exception
     */
    public static ApiResponse request(String serverRoot,
                                      YopRequestType requestType, YopRequestMethod httpMethod, String apiUri,
                                      YopRequestContentType contentType, Map<String, Object> queryParams,
                                      Map<String, Object> bodyParams, Set<String> encryptParams,
                                      String yopPublicKey, String isvPrivateKey) throws Exception {
        // header 不加密
        Set<String> encryptHeaders = Collections.EMPTY_SET;

        // 非法参数过滤
        queryParams = autoFixParams(queryParams);
        bodyParams = autoFixParams(bodyParams);


        // 敏感业务参数加密
        String encryptKey = null;
        Map<String, Object> handledQueryParams = queryParams;
        Object handledBodyParams = contentType == YopRequestContentType.JSON ? toJsonString(bodyParams) : bodyParams;
        Set<String> handledEncryptParams = new HashSet<>();
        if (encryptParams != null) {
            encryptKey = encodeBase64(generateRandomKey());
            handledQueryParams = new HashMap<>();
            for (String key : queryParams.keySet()) {
                if (encryptParams.contains(key)) {
                    handledQueryParams.put(key, encryptParam(encryptKey, queryParams.get(key).toString()));
                    handledEncryptParams.add(key);
                } else {
                    handledQueryParams.put(key, queryParams.get(key));
                }
            }
            // json请求，整体加密
            if (contentType == YopRequestContentType.JSON) {
                handledBodyParams = toJsonString(encryptParam(encryptKey, (String) handledBodyParams));
                handledEncryptParams.clear();
                handledEncryptParams.add("$");
            } else {
                Map<String, Object> tmpBodyParams = new HashMap<>();
                for (String key : bodyParams.keySet()) {
                    Object value = bodyParams.get(key);
                    if (encryptParams.contains(key)) {
                        // 仅加密非文件参数
                        if (!isFileParam(value)) {
                            tmpBodyParams.put(key, encryptParam(encryptKey, value.toString()));
                            handledEncryptParams.add(key);
                        } else {
                            tmpBodyParams.put(key, value);
                        }
                    } else {
                        tmpBodyParams.put(key, value);
                    }
                }
                handledBodyParams = tmpBodyParams;
            }
        }

        // 构建认证信息
        Map<String, String> headers = buildAuthHeaders(httpMethod, apiUri, contentType, handledQueryParams, handledBodyParams,
                encryptKey, encryptHeaders, handledEncryptParams, yopPublicKey, isvPrivateKey);

        // 构建http请求
        HttpUriRequest httpUriRequest = buildHttpRequest(serverRoot + apiUri, httpMethod, contentType,
                headers, handledQueryParams, handledBodyParams);

        // 发送http请求
        HttpResponse response = httpClient.execute(httpUriRequest);

        // 处理API响应
        ApiResponse apiResponse = handleResponse(requestType, response, yopPublicKey, encryptKey);
        return apiResponse;
    }

    private static Map<String, Object> autoFixParams(Map<String, Object> params) {
        if (null == params || params.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> result = new HashMap<>();
        params.forEach((key, value) -> {
            if (null != key && null!= value) {
                result.put(key, value);
            }
        });
        return result;
    }

    /**
     * 处理API响应
     *
     * @param requestType  请求类型
     * @param response    HTTP响应
     * @param publicKey   平台公钥
     * @param encryptKey  加密会话密钥
     * @return API响应对象
     */
    public static ApiResponse handleResponse(YopRequestType requestType, HttpResponse response, String publicKey, String encryptKey) {
        try {
            // 1. 获取状态码
            int statusCode = response.getStatusLine().getStatusCode();

            // 2. 获取响应头
            Map<String, String> headers = new HashMap<>();
            for (Header header : response.getAllHeaders()) {
                headers.put(header.getName(), header.getValue());
            }
            String signature = headers.get(YOP_SIGN);
            String requestId = headers.get(YOP_REQUEST_ID);
            String encrypt = headers.get(YOP_ENCRYPT);

            // 3. 获取响应内容
            HttpEntity entity = response.getEntity();
            if (requestType == YopRequestType.FILE_DOWNLOAD) {
                // 文件下载响应
                return handleFileDownloadResponse(headers, response, requestId, encrypt, encryptKey);
            }
            String responseContent = EntityUtils.toString(entity, DEFAULT_ENCODING);

            // 4. 验证响应签名（如果有）
            if (signature != null && !signature.isEmpty()) {
                boolean validSignature = verifyResponseSignature(responseContent, signature, publicKey);
                if (!validSignature) {
                    throw new SecurityException("响应签名验证失败");
                }
            }

            // 5. 解析响应内容
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatusCode(statusCode);
            apiResponse.setRequestId(requestId);
            apiResponse.setRawContent(responseContent);

            // 6. 根据状态码处理
            if (statusCode == 200) {
                // 成功响应
                apiResponse.setSuccess(true);
                apiResponse.setResult(parseSuccessResponse(responseContent, encrypt, encryptKey));
            } else if (statusCode == 500) {
                // 业务错误
                apiResponse.setSuccess(false);
                apiResponse.setError(parseErrorResponse(responseContent));
            } else {
                // 其他错误
                apiResponse.setSuccess(false);
                ApiError error = new ApiError();
                error.setCode("SYSTEM_ERROR");
                error.setMessage("Unexpected status code: " + statusCode);
                apiResponse.setError(error);
            }

            return apiResponse;
        } catch (Exception e) {
            throw new RuntimeException("处理响应失败", e);
        }
    }

    private static ApiResponse handleFileDownloadResponse(Map<String, String> rspHeaders, HttpResponse response, String requestId, String encrypt, String encryptKey) throws Exception {
        int statusCode = response.getStatusLine().getStatusCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatusCode(statusCode);
        apiResponse.setRequestId(requestId);

        if (statusCode == 200 && isDownloadResponse(response)) {
            // 成功响应
            apiResponse.setSuccess(true);
            InputStream rawContent;
            if (null != encrypt && null != encryptKey) {
                rawContent = decryptStream(encryptKey, response.getEntity().getContent());
            } else {
                rawContent = response.getEntity().getContent();
            }
            apiResponse.setRawContent(rawContent);
            File tmpFile = saveFile(rawContent, rspHeaders);
            apiResponse.setResult(tmpFile);
        } else if (statusCode == 500) {
            // 业务错误
            apiResponse.setSuccess(false);
            apiResponse.setError(parseErrorResponse(EntityUtils.toString(response.getEntity(), DEFAULT_ENCODING)));
        } else {
            apiResponse.setSuccess(false);
            apiResponse.setRawContent(EntityUtils.toString(response.getEntity(), DEFAULT_ENCODING));
            ApiError error = new ApiError();
            error.setCode("SYSTEM_ERROR");
            error.setMessage("Unexpected status code: " + statusCode);
            apiResponse.setError(error);
        }
        return apiResponse;
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
                if (isNotBlank(fileName)) {
                    final String[] split = fileName.split("\\.");
                    if (split.length == 2) {
                        if (strLength(split[0])  > 3) {
                            filePrefix = split[0];
                        }
                        if (isNotBlank(split[1])) {
                            fileSuffix = "." + split[1];
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("parse Content-Disposition fail, value:{}, ex:",  contentDisposition, e);
            }
            File tmpFile = File.createTempFile(filePrefix, fileSuffix);
            long fileSize = copyStream(fileContent, Files.newOutputStream(tmpFile.toPath()));
            LOGGER.info("downloaded file, name:{}, size:{}", tmpFile.getName(), fileSize);
            return tmpFile;
        } catch (Throwable ex) {
            throw new RuntimeException("fail to save file", ex);
        } finally {
            closeQuietly(fileContent);
        }
    }

    private static long copyStream(final InputStream input, final OutputStream output)
            throws IOException {
        long count = 0;
        if (input != null) {
            byte[] buffer = new byte[8192];
            int n;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        }
        return count;
    }

    private static boolean isNotBlank(String str) {
        return null != str && !str.trim().isEmpty();
    }

    private static int strLength(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    private static String strTrim(final String str) {
        return str == null ? null : str.trim();
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
        if (!isNotBlank(contentDisposition)) {
            return null;
        }

        final String[] parts = contentDisposition.split( "filename=");
        if (parts.length == 2) {
            return strTrim(parts[1]);
        }
        return null;
    }

    private static boolean isDownloadResponse(HttpResponse response) {
        return null != response.getEntity() && null != response.getEntity().getContentType() &&
                response.getEntity().getContentType().getValue().startsWith(YOP_HTTP_CONTENT_TYPE_STREAM);
    }

    private static String decryptIfNecessary(String responseContent, String encryptHeader) {
        if (null == encryptHeader) {
            return responseContent;
        }

        return null;
    }

    /**
     * 解析错误响应
     *
     * 须引入第三方包 com.fasterxml.jackson.core:jackson-databind
     * @param content 响应内容
     * @return 错误对象
     */
    private static ApiError parseErrorResponse(String content) throws Exception {
        JsonNode rootNode = OBJECT_MAPPER.readTree(content);

        ApiError error = new ApiError();

        // 提取错误信息
        if (rootNode.has("code")) {
            error.setCode(rootNode.get("code").asText());
        }

        if (rootNode.has("message")) {
            error.setMessage(rootNode.get("message").asText());
        }

        if (rootNode.has("subCode")) {
            error.setSubCode(rootNode.get("subCode").asText());
        }

        if (rootNode.has("subMessage")) {
            error.setSubMessage(rootNode.get("subMessage").asText());
        }

        return error;
    }

    /**
     * 解析成功响应
     *
     * 须引入第三方包 com.fasterxml.jackson.core:jackson-databind
     * @param content 响应内容
     * @param encryptHeader 响应加密头
     * @param encryptKey 加密会话密钥
     * @return 解析后的结果对象
     */
    private static Object parseSuccessResponse(String content, String encryptHeader, String encryptKey) throws Exception {
        JsonNode rootNode = OBJECT_MAPPER.readTree(content);

        // 获取result节点
        JsonNode resultNode = rootNode.get("result");
        if (resultNode != null) {
            if (null != encryptHeader && null != encryptKey) {
                String decryptBizData = decryptParam(encryptKey, resultNode.asText());
                return OBJECT_MAPPER.readValue(decryptBizData, Object.class);
            }
            return OBJECT_MAPPER.convertValue(resultNode, Object.class);
        }

        // 没有result节点
        throw new RuntimeException("Unexpected响应字符串：" + content);
    }

    private static String decryptParam(String aesKey, String encryptContent) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.DECRYPT_MODE, aesKey);
        // 返回的json有格式化，需要去掉后再解码
        return new String(cipher.doFinal(decodeBase64(urlSafeDecode(encryptContent.replaceAll("[\r\n]", EMPTY)))), DEFAULT_ENCODING);
    }

    private static InputStream decryptStream(String aesKey, InputStream encryptStream) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.DECRYPT_MODE, aesKey);
        return new CipherInputStream(encryptStream, cipher);
    }

    /**
     * 验证响应签名
     *
     * @param content 响应内容
     * @param signature 响应签名（来自x-yop-sign头）
     * @param publicKeyBase64Str 平台公钥(base64格式)
     * @return 验证结果
     */
    public static boolean verifyResponseSignature(
            String content,
            String signature,
            String publicKeyBase64Str) {

        try {
            // 1. 准备验证数据（移除空白字符）
            String normalizedContent = content.replaceAll("[ \t\n]", EMPTY);
            byte[] contentBytes = normalizedContent.getBytes(DEFAULT_ENCODING);

            // 2. 解析签名
            String signatureValue = signature;
            String algorithm = "SHA256";

            // 如果签名包含算法标识，分离出来
            if (signature.contains("$")) {
                String[] parts = signature.split("\\$");
                signatureValue = parts[0];
                algorithm = parts[1];
            }

            // 3. 解码签名（URL安全Base64 -> 标准Base64 -> 二进制）
            String standardBase64 = urlSafeDecode(signatureValue);
            byte[] signatureBytes = decodeBase64(standardBase64);

            // 4. 加载公钥
            PublicKey publicKey = string2PublicKey(publicKeyBase64Str);

            // 5. 验证签名
            Signature verifier = Signature.getInstance(DEFAULT_SIGN_ALG);
            verifier.initVerify(publicKey);
            verifier.update(contentBytes);

            return verifier.verify(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("验证响应签名失败", e);
        }
    }

    /**
     * 构造HttpRequest
     *
     * @param requestUrl    请求URL
     * @param requestMethod 请求方法
     * @param contentType   请求体格式
     * @param headers       请求头
     * @param queryParams   请求URL参数
     * @param bodyParams    请求体参数
     * @return
     * @throws UnsupportedEncodingException
     */
    public static HttpUriRequest buildHttpRequest(String requestUrl,
                                                     YopRequestMethod requestMethod,
                                                     YopRequestContentType contentType,
                                                     Map<String, String> headers,
                                                     Map<String, Object> queryParams,
                                                     Object bodyParams)
            throws UnsupportedEncodingException, JsonProcessingException, FileNotFoundException {
        // 3. 创建请求
        HttpRequestBase request;

        if (requestMethod == YopRequestMethod.POST) {
            // POST请求
            HttpPost post = new HttpPost(requestUrl);
            if (null != bodyParams) {
                if (contentType == YopRequestContentType.JSON) {
                    // application/json格式请求体
                    StringEntity entity = new StringEntity((String) bodyParams, DEFAULT_ENCODING);
                    entity.setContentType(contentType.getValue());
                    post.setEntity(entity);
                    post.addHeader(CONTENT_TYPE, contentType.getValue());
                } else if (contentType == YopRequestContentType.MULTIPART_FORM) {
                    // multipart/form-data格式请求体
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    for (Map.Entry<String, Object> entry : ((Map<String, Object>) bodyParams).entrySet()) {
                        String name = entry.getKey();
                        Object value = entry.getValue();
                        if (value instanceof FileParam) {
                            // 文件包装类参数
                            FileParam file = (FileParam) value;
                            builder.addBinaryBody(name, file.getInputStream(),
                                    ContentType.DEFAULT_BINARY, file.getFilaName());
                        } else if (value instanceof File) {
                            // 文件参数
                            File file = (File) value;
                            builder.addBinaryBody(name, new FileInputStream(file), ContentType.DEFAULT_BINARY, file.getName());
                        } else if (value instanceof InputStream) {
                            // 流文件参数
                            builder.addBinaryBody(name, (InputStream) value, ContentType.DEFAULT_BINARY, randomFileName());
                        } else {
                            // 普通字符串参数，注意，此处需要urlEncode两次
                            builder.addTextBody(name, urlEncodeForSign(value.toString()), ContentType.TEXT_PLAIN);
                        }
                        post.setEntity(builder.build());
                    }
                } else {
                    // application/x-www-form-urlencoded格式请求体，注意，此处需要urlEncode两次(手动一次+httpclient处理一次)
                    List<NameValuePair> formParams = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : ((Map<String, Object>) bodyParams).entrySet()) {
                        formParams.add(new BasicNameValuePair(entry.getKey(), URLEncoder.encode(entry.getValue().toString(), DEFAULT_ENCODING)));
                    }
                    post.setEntity(new UrlEncodedFormEntity(formParams, DEFAULT_ENCODING));
                    post.addHeader(CONTENT_TYPE, YopRequestContentType.FORM_URL_ENCODE.getValue());
                }
            }
            request = post;
        } else {
            // GET请求
            StringBuilder urlWithParams = new StringBuilder(requestUrl);

            // 添加查询参数
            if (queryParams != null && !queryParams.isEmpty()) {
                urlWithParams.append("?");
                List<String> paramPairs = new ArrayList<>();

                for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                    String encodedName = URLEncoder.encode(entry.getKey(), DEFAULT_ENCODING);
                    // 注意，此处需要urlEncode两次
                    String encodedValue = URLEncoder.encode(entry.getValue().toString(), DEFAULT_ENCODING);
                    paramPairs.add(encodedName + "=" + URLEncoder.encode(encodedValue, DEFAULT_ENCODING));
                }

                urlWithParams.append(String.join("&", paramPairs));
            }

            request = new HttpGet(urlWithParams.toString());
            request.addHeader(CONTENT_TYPE, YopRequestContentType.FORM_URL_ENCODE.getValue());
        }

        // 4. 添加请求头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        return request;
    }

    private static byte[] generateRandomKey() throws NoSuchAlgorithmException {
        //实例化
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        //设置密钥长度
        generator.init(128);
        //生成密钥
        return generator.generateKey().getEncoded();
    }


    private static Map<String, String> buildAuthHeaders(YopRequestMethod httpMethod,
                                                        String apiUri,
                                                        YopRequestContentType contentType,
                                                        Map<String, Object> queryParams,
                                                        Object bodyParams,
                                                        String encryptKey, Set<String> encryptHeaders, Set<String> encryptParams,
                                                        String yopPublicKey, String isvPrivateKey) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(YOP_SDK_LANGS, "java");
        headers.put(YOP_SDK_VERSION, "4.0.0");
        headers.put(YOP_APPKEY, APP_KEY);
        headers.put(YOP_REQUEST_ID, UUID.randomUUID().toString());

        // 加密头
        if (null != encryptKey && null != encryptParams) {
            headers.put(YOP_ENCRYPT, buildEncryptHeader(encryptHeaders, encryptParams, encryptKey, yopPublicKey));
        }

        // 摘要头
        headers.put(YOP_CONTENT_SHA256, calculateContentSha256(httpMethod, contentType == YopRequestContentType.JSON, bodyParams));

        // 签名头
        headers.put(AUTHORIZATION, signRequest(httpMethod, apiUri, contentType, headers, queryParams, isvPrivateKey));
        return headers;
    }

    private static String signRequest(YopRequestMethod requestMethod, String requestUri,
                                      YopRequestContentType contentType,
                                      Map<String, String> headers,
                                      Map<String, Object> queryParams,
                                      String isvPrivateKey) throws Exception {
        // 1.构造认证字符串
        String authString = buildAuthString(APP_KEY, 1800);

        // 2.获取规范请求方法
        String httpRequestMethod = requestMethod.name();

        // 3.获取规范请求URI
        String canonicalURI = requestUri;

        // 4.获取规范化查询字符串
        String canonicalQueryString = buildCanonicalQueryString(requestMethod, contentType == YopRequestContentType.JSON,
                queryParams);

        // 5.获取规范请求头
        String canonicalHeaders = buildCanonicalHeaders(headers);

        // 6.获取规范请求串
        StringBuilder canonicalRequest = new StringBuilder()
                .append(authString)
                .append("\n")
                .append(httpRequestMethod)
                .append("\n")
                .append(canonicalURI)
                .append("\n")
                .append(canonicalQueryString)
                .append("\n")
                .append(canonicalHeaders);


        // 7.计算签名
        String signature = urlSafeEncode(encodeBase64(sign(canonicalRequest.toString().getBytes(DEFAULT_ENCODING), isvPrivateKey))) + "$" + "SHA256";

        // 8.构建认证头
        return buildAuthorizationHeader(authString, String.join(SEMICOLON, HEADERS_TO_SIGN), signature);
    }

    /**
     * 构建认证头
     *
     * @param authString 认证字符串
     * @param signedHeaders 已签名的请求头
     * @param signature 签名结果
     * @return 认证头值
     */
    public static String buildAuthorizationHeader(String authString, String signedHeaders, String signature) {
        return DEFAULT_AUTH_PREFIX_RSA2048 + " " +
                authString + SLASH +
                signedHeaders + SLASH +
                signature;
    }

    // 签名计算时至少包含这三个头
    private static final List<String> HEADERS_TO_SIGN = Arrays.asList(
            "x-yop-appkey",
            "x-yop-content-sha256",
            "x-yop-request-id"
    );

    /**
     * 构建规范请求头
     */
    private static String buildCanonicalHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return EMPTY;
        }

        // 1. 转换头名称为小写并排序
        List<String> canonicalHeaders = new ArrayList<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String headerName = entry.getKey().toLowerCase().trim();
            String headerValue = entry.getValue().trim();
            if (HEADERS_TO_SIGN.contains(headerName)) {
                canonicalHeaders.add(headerName + ":" + headerValue);
            }
        }
        // 2. 排序
        Collections.sort(canonicalHeaders);

        // 3. 用\n连接所有签名头
        return String.join("\n", canonicalHeaders);
    }

    /**
     * 构建规范查询字符串
     *
     * @param queryParams 请求query参数
     * @return 规范查询字符串
     */
    private static String buildCanonicalQueryString(YopRequestMethod httpMethod,
                                             boolean isJsonRequest,
                                             Map<String, Object> queryParams) {
        // 针对POST请求，且内容类型为form，即application/x-www-form-urlencoded或者multipart/form-data的请求，返回空字符串
        if (httpMethod == YopRequestMethod.POST && !isJsonRequest) {
            return EMPTY;
        }
        // 参数编码&排序
        return sortAndEncodeParams(queryParams);
    }

    public static byte[] sign(byte[] data, String isvPrivateKey) {
        try {
            Signature signature = Signature.getInstance(DEFAULT_SIGN_ALG);
            signature.initSign(string2PrivateKey(isvPrivateKey));
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Sign Fail, key:" + ISV_PRIVATE_KEY + ", ex:", e);
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

    /**
     * 构建认证字符串
     *
     * @param appKey 应用标识
     * @param expiredSeconds 过期时间（秒）
     * @return 认证字符串
     */
    public static String buildAuthString(String appKey, int expiredSeconds) {
        // 1. 协议版本
        String protocolVersion = DEFAULT_YOP_PROTOCOL_VERSION + SLASH;

        // 2. 应用标识
        String appKeyPart = appKey + SLASH;

        // 3. ISO8601格式时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = sdf.format(new Date()) + SLASH;

        // 4. 过期时间（秒）
        String expired = String.valueOf(expiredSeconds);

        // 5. 组合认证字符串
        return protocolVersion + appKeyPart + timestamp + expired;
    }

    /**
     * 计算请求摘要头
     */
    private static String calculateContentSha256(YopRequestMethod httpMethod,
                                                 boolean isJsonRequest,
                                                 Object bodyParams) {
        // 针对GET请求，计算为空字符串
        if (httpMethod == YopRequestMethod.GET) {
            return sha256ToHex(EMPTY);
        }

        // 针对POST-JSON格式请求，计算整个json请求体
        if (isJsonRequest) {
            return sha256ToHex((String) bodyParams);
        }

        // 其他格式请求，仅计算非文件类参数
        Map<String, Object> nonFileParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) bodyParams).entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null && !isFileParam(value)) {
                nonFileParams.put(key, value.toString());
            }
        }
        return sha256ToHex(sortAndEncodeParams(nonFileParams));
    }

    /**
     * 参数编码&排序
     */
    private static String sortAndEncodeParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return EMPTY;
        }

        // 1. 参数排序（按键名ASCII升序）
        List<String> paramNames = new ArrayList<>(params.keySet());
        Collections.sort(paramNames);

        // 2. 构建规范查询字符串
        List<String> queryParts = new ArrayList<>();
        for (String name : paramNames) {
            String value = params.get(name).toString();
            // URL编码参数名和值
            String encodedName = urlEncodeForSign(name);
            String encodedValue = urlEncodeForSign(value);
            queryParts.add(encodedName + "=" + encodedValue);
        }

        // 3. 用&连接所有参数对
        return String.join("&", queryParts);
    }

    /**
     * URL编码(签名版)
     */
    private static String urlEncodeForSign(String value) {
        try {
            return URLEncoder.encode(value, DEFAULT_ENCODING)
                    .replace("+", "%20")
                    .replace("*", "%2A")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    private static boolean isFileParam(Object value) {
        return null != value && (value instanceof File || value instanceof InputStream || value instanceof FileParam);
    }

    /**
     * 转json字符串
     */
    private static String toJsonString(Object bodyParams) {
        try {
            return OBJECT_MAPPER.writeValueAsString(bodyParams);
        } catch (Exception e) {
            throw new RuntimeException("toJsonString fail", e);
        }
    }

    /**
     * 计算sha256并转16进制字符串
     */
    private static String sha256ToHex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance(DEFAULT_DIGEST_ALG);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes(DEFAULT_ENCODING));
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, md);
            byte[] buffer = new byte[1024];
            while (digestInputStream.read(buffer) > -1) {
            }
            byte[] digestBytes = digestInputStream.getMessageDigest().digest();
            return encodeHex(digestBytes);
        } catch (Exception e) {
            throw new RuntimeException("sha256 fail", e);
        }
    }

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static String encodeHex(byte[] data) {
        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for (int j = 0; i < l; ++i) {
            out[j++] = DIGITS[(240 & data[i]) >>> 4];
            out[j++] = DIGITS[15 & data[i]];
        }

        return new String(out);
    }

    /**
     * 加密协议头(请求)：
     * <p>
     * yop-encrypt-v1/{服务端证书序列号}/{密钥类型(必填)}_{分组模式(必填)}_{填充算法(必填)}/{加密密钥值(必填)}/{IV}{;}{附加信息}/{客户端支持的大参数加密模式(必填)}/{encryptHeaders}/{encryptParams}
     */
    public static String buildEncryptHeader(Set<String> encryptHeaders, Set<String> encryptParams,
                                            String encryptKey, String yopPublicKey) throws Exception {
        if (null == encryptHeaders) {
            encryptHeaders = Collections.emptySet();
        }
        if (null == encryptParams) {
            encryptParams = Collections.emptySet();
        }

        return "yop-encrypt-v1" + SLASH +
                EMPTY + SLASH + //rsa 为空
                AES_ENCRYPT_ALG.replace(SLASH, UNDER_LINE) + SLASH +
                urlSafeEncode(encodeBase64(encryptKey(decodeBase64(encryptKey), string2PublicKey(yopPublicKey)))) + SLASH +
                EMPTY + SLASH +
                STREAM + SLASH +
                String.join(SEMICOLON, encryptHeaders) + SLASH +
                urlSafeEncode(encodeBase64(String.join(SEMICOLON, encryptParams).getBytes(DEFAULT_ENCODING)));
    }

    private static byte[] encryptKey(byte[] data, Key key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(RSA_ENCRYPT_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("SystemError, Encrypt Fail, key:"
                    + key + ", cause:" + e.getMessage(), e);
        }
    }

    private static String encryptParam(String aesKey, String plain) throws Exception {
        final Cipher cipher = getInitializedCipher(Cipher.ENCRYPT_MODE, aesKey);
        return urlSafeEncode(encodeBase64(cipher.doFinal(plain.getBytes(DEFAULT_ENCODING))));
    }

    private static Cipher getInitializedCipher(int mode, String aesKey) {
        try {
            byte[] key = decodeBase64(aesKey);
            Cipher cipher = Cipher.getInstance(AES_ENCRYPT_ALG);
            Key secretKey = new SecretKeySpec(key, "AES");
            cipher.init(mode, secretKey);
            return cipher;
        } catch (Throwable throwable) {
            throw new RuntimeException("error happened when initialize cipher", throwable);
        }
    }

    public static PublicKey string2PublicKey(String pubKey) {
        try {
            return KeyFactory.getInstance(RSA).generatePublic(
                    new X509EncodedKeySpec(decodeBase64(pubKey)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("ConfigProblem, YopPublicKey ParseFail, value:" + pubKey + ", ex:", e);
        }
    }

    /**
     * Base64 URL安全替换
     * 将标准Base64中的'+'替换为'-'，'/'替换为'_'
     *
     * @param unSafeBase64Str 标准Base64编码字符串
     * @return URL安全的Base64编码字符串
     */
    public static String urlSafeEncode(String unSafeBase64Str) {
        if (unSafeBase64Str == null) {
            return null;
        }
        return unSafeBase64Str.replace('+', '-')
                .replace('/', '_');
    }

    /**
     * Base64 URL解码转换
     * 将URL安全Base64中的'-'替换回'+'，'_'替换回'/'
     *
     * @param safeBase64Str URL安全的Base64编码字符串
     * @return 标准Base64编码字符串
     */
    public static String urlSafeDecode(String safeBase64Str) {
        if (safeBase64Str == null) {
            return null;
        }
        return safeBase64Str.replace('-', '+')
                .replace('_', '/');
    }

    /**
     * Base64 解码(非url安全)
     *
     * @param input
     * @return
     */
    public static byte[] decodeBase64(String input) {
        return Base64.getDecoder().decode(input);
    }

    /**
     * Base64 编码(非url安全)
     *
     * @param input
     * @return
     */
    public static String encodeBase64(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }

    public enum YopRequestMethod {
        GET,
        POST
    }

    public enum YopRequestType {
        /**
         * 普通请求，json返回
         */
        WEB,
        /**
         * 文件下载请求，返回文件流
         */
        FILE_DOWNLOAD,

        /**
         * 文件上传请求，返回json
         */
        MULTI_FILE_UPLOAD,
    }

    private static final String YOP_HTTP_CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String YOP_HTTP_CONTENT_TYPE_FORM = "application/x-www-form-urlencoded;charset=UTF-8";
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

    private static String randomFileName() {
        return UUID.randomUUID() + ".bin";
    }

    public static class FileParam implements Serializable {
        private static final long serialVersionUID = -1L;

        // 文件流
        private InputStream inputStream;

        // 文件名
        private String filaName = randomFileName();

        public FileParam(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        // 本地文件
        public FileParam(File file) throws FileNotFoundException {
            this.inputStream = new FileInputStream(file);
            this.filaName = file.getName();
        }

        // 文件流
        public FileParam(InputStream inputStream, String filaName) {
            this.inputStream = inputStream;
            this.filaName = filaName;
        }

        // 远端文件
        public FileParam(URL url, String filaName) throws IOException {
            this.inputStream = url.openStream();
            this.filaName = filaName;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public String getFilaName() {
            return filaName;
        }
    }

    public static class ApiResponse implements Serializable {
        private static final long serialVersionUID = -1L;

        private int statusCode;
        private String requestId;
        private Object rawContent;
        private boolean success;
        private Object result;
        private ApiError error;

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public Object getRawContent() {
            return rawContent;
        }

        public void setRawContent(Object rawContent) {
            this.rawContent = rawContent;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public ApiError getError() {
            return error;
        }

        public void setError(ApiError error) {
            this.error = error;
        }

        @Override
        public String toString() {
            return "ApiResponse{" +
                    "statusCode=" + statusCode +
                    ", requestId='" + requestId + '\'' +
                    ", rawContent='" + rawContent + '\'' +
                    ", success=" + success +
                    ", result=" + result +
                    ", error=" + error +
                    '}';
        }
    }

    public static class ApiError implements Serializable {
        private static final long serialVersionUID = -1L;
        private String code;
        private String message;
        private String subCode;
        private String subMessage;

        public ApiError() {
        }

        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getSubCode() {
            return subCode;
        }

        public void setSubCode(String subCode) {
            this.subCode = subCode;
        }

        public String getSubMessage() {
            return subMessage;
        }

        public void setSubMessage(String subMessage) {
            this.subMessage = subMessage;
        }
    }
}
