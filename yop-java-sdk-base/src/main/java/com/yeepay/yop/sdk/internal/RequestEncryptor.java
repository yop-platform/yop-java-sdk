/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.internal;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptProtocol;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.utils.Encodes;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import static com.yeepay.yop.sdk.http.Headers.YOP_ENCRYPT;
import static com.yeepay.yop.sdk.security.encrypt.YopEncryptProtocol.YOP_ENCRYPT_PROTOCOL_V1_REQ;
import static com.yeepay.yop.sdk.utils.CharacterConstants.*;
import static com.yeepay.yop.sdk.utils.JsonUtils.resolveAllJsonPaths;

/**
 * title: 负责加密YopRequest<br>
 * description: 底层调用加密器<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/13
 */
public class RequestEncryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestEncryptor.class);

    /**
     * 加密并重写Request
     *
     * @param request 请求
     * @param encryptor 加密器
     * @param encryptOptions 加密选项
     */
    public static void encrypt(Request<? extends BaseRequest> request, YopEncryptor encryptor, EncryptOptions encryptOptions)
            throws UnsupportedEncodingException {
        YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        List<String> encryptHeaders = Collections.emptyList();
        List<String> encryptParams = Collections.emptyList();
        if (BooleanUtils.isTrue(requestConfig.getNeedEncrypt())) {
            encryptHeaders = encryptHeaders(encryptor, requestConfig.getEncryptHeaders(), request, encryptOptions);
            encryptParams = encryptParams(encryptor, requestConfig.getEncryptParams(), request, requestConfig, encryptOptions);
        }

        // 请求参数不加密的情况, 也需设置加密头，用于响应结果加密
        buildEncryptHeader(request, encryptHeaders, encryptParams, encryptOptions);
    }



    /**
     * 加密协议头
     *
     * @see YopEncryptProtocol#YOP_ENCRYPT_PROTOCOL_V1_REQ
     */
    public static String buildEncryptHeader(Request<? extends BaseRequest> request, List<String> encryptHeaders,
                                            List<String> encryptParams, EncryptOptions encryptOptions) throws UnsupportedEncodingException {
        String encryptHeader =  YOP_ENCRYPT_PROTOCOL_V1_REQ.getProtocolPrefix() + SLASH +
                StringUtils.replace(encryptOptions.getAlg(), SLASH, UNDER_LINE) + SLASH +
                encryptOptions.getEncryptedCredentials() + SLASH +
                encryptOptions.getIv() + SEMICOLON +
                encryptOptions.getAad() + SLASH +
                encryptOptions.getBigParamEncryptMode() + SLASH +
                StringUtils.join(encryptHeaders, SEMICOLON) + SLASH +
                Encodes.encodeUrlSafeBase64(StringUtils.join(encryptParams, SEMICOLON).getBytes(YopConstants.DEFAULT_ENCODING));
        LOGGER.debug("encryptHeader:{}", encryptHeader);

        // 添加加密头
        request.addHeader(YOP_ENCRYPT, encryptHeader);
        return encryptHeader;
    }

    private static List<String> encryptHeaders(YopEncryptor encryptor, List<String> encryptHeaders,
                                        Request<? extends BaseRequest> request, EncryptOptions encryptOptions) {
        if (CollectionUtils.isEmpty(encryptHeaders)) {
            return encryptHeaders;
        }
        List<String> finalEncryptHeaders = Lists.newArrayListWithExpectedSize(encryptHeaders.size());
        Map<String, String> headers = request.getHeaders();
        headers.forEach((k,v) -> {
            if (encryptHeaders.contains(k) && StringUtils.isNotBlank(v)) {
                headers.put(k, encryptor.encryptToBase64(v, encryptOptions));
                finalEncryptHeaders.add(k);
            }
        });
        return finalEncryptHeaders;
    }

    private static List<String> encryptParams(YopEncryptor encryptor, List<String> encryptParams, Request<? extends BaseRequest> request,
                                       YopRequestConfig requestConfig, EncryptOptions encryptOptions) {
        if (CollectionUtils.isEmpty(encryptParams) && null == request.getContent()) {
            return encryptParams;
        }

        List<String> finalEncryptParams = Lists.newArrayListWithExpectedSize(encryptParams.size());
        Map<String, List<String>> parameters = request.getParameters();
        encryptCommonParams(encryptor, finalEncryptParams, encryptParams, parameters, encryptOptions);

        Map<String, List<MultiPartFile>> multiPartFiles = request.getMultiPartFiles();
        encryptMultiPartParams(encryptor, finalEncryptParams, encryptParams, multiPartFiles, encryptOptions);

        encryptContent(encryptor, finalEncryptParams, request, requestConfig, encryptOptions);
        return finalEncryptParams;
    }

    private static void encryptContent(YopEncryptor encryptor, List<String> finalEncryptParams, Request<? extends BaseRequest> request,
                                YopRequestConfig requestConfig, EncryptOptions encryptOptions) {
        if (null == request.getContent()) return;

        if (YopContentType.JSON.equals(request.getContentType())) {
            byte[] jsonBytes = encryptJsonParams(encryptor, finalEncryptParams, requestConfig, request.getContent(), encryptOptions);
            RestartableInputStream restartableInputStream = RestartableInputStream.wrap(jsonBytes);
            request.setContent(restartableInputStream);
            // ！！！覆盖掉原文计算的length，否则httpClient会用原文指定的length头来发送报文，导致完整性校验不通过
            request.addHeader(Headers.CONTENT_LENGTH, String.valueOf(jsonBytes.length));
        } else if (YopContentType.OCTET_STREAM.equals(request.getContentType())){
            request.setContent(encryptor.encrypt(request.getContent(), encryptOptions));
            finalEncryptParams.add(DOLLAR);
        } else {
            throw new YopClientException("body content is not supported, contentType:" + request.getContentType());
        }
    }

    private static byte[] encryptJsonParams(YopEncryptor encryptor, List<String> finalEncryptParams, YopRequestConfig requestConfig,
                                          InputStream content, EncryptOptions encryptOptions) {
        try {
            String originJson = IOUtils.toString(content, YopConstants.DEFAULT_ENCODING);
            String encryptedJson;
            if (BooleanUtils.isFalse(requestConfig.getTotalEncrypt())) {
                SortedSet<String> encryptPaths = resolveAllJsonPaths(originJson, requestConfig.getEncryptParams());

                DocumentContext valReadWriteCtx = JsonPath.parse(originJson);
                for (String encryptPath : encryptPaths) {
                    try {
                        String plainVal = JsonUtils.toJsonString(valReadWriteCtx.read(encryptPath));
                        if (StringUtils.isNotBlank(plainVal)) {
                            valReadWriteCtx.set(encryptPath, encryptor.encryptToBase64(plainVal, encryptOptions));
                            finalEncryptParams.add(encryptPath);
                        }
                    } catch (PathNotFoundException e) {
                        // ignore 加密父节点后，字节点会找不到
                    }
                }
                encryptedJson = valReadWriteCtx.jsonString();
            } else {
                finalEncryptParams.add(DOLLAR);
                encryptedJson = JsonUtils.toJsonString(encryptor.encryptToBase64(originJson, encryptOptions));
            }
            return encryptedJson.getBytes(YopConstants.DEFAULT_ENCODING);
        } catch (IOException e) {
            throw new YopClientException("error happened when encrypt json", e);
        }
    }

    private static void encryptMultiPartParams(YopEncryptor encryptor, List<String> finalEncryptParams, List<String> encryptParams,
                                        Map<String, List<MultiPartFile>> multiPartFiles, EncryptOptions encryptOptions) {
        multiPartFiles.forEach((name, list) -> {
            if (encryptParams.contains(name) && CollectionUtils.isNotEmpty(list)) {
                List<MultiPartFile> encryptedValues = Lists.newArrayListWithExpectedSize(list.size());
                for (MultiPartFile value : list) {
                    try {
                        encryptedValues.add(new MultiPartFile(
                                encryptor.encrypt(value.getInputStream(), encryptOptions), value.getFileName()));
                    } catch (IOException e) {
                        throw new YopClientException("error happened when encrypt MultiPartFile", e);
                    }
                }
                multiPartFiles.put(name, encryptedValues);
                finalEncryptParams.add(name);
            }
        });
    }

    private static void encryptCommonParams(YopEncryptor encryptor, List<String> finalEncryptParams, List<String> encryptParams,
                                     Map<String, List<String>> parameters, EncryptOptions encryptOptions) {
        parameters.forEach((name, list) -> {
            if (encryptParams.contains(name) && CollectionUtils.isNotEmpty(list)) {
                List<String> encryptedValues = Lists.newArrayListWithExpectedSize(list.size());
                for (String value : list) {
                    encryptedValues.add(encryptor.encryptToBase64(value, encryptOptions));
                }
                parameters.put(name, encryptedValues);
                finalEncryptParams.add(name);
            }
        });
    }
}
