/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.base.cache.EncryptOptionsCache;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptProtocol;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
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
import java.util.Set;
import java.util.concurrent.Future;

import static com.yeepay.yop.sdk.YopConstants.YOP_ENCRYPT_OPTIONS_YOP_PLATFORM_CERT_SERIAL_NO;
import static com.yeepay.yop.sdk.base.security.encrypt.YopEncryptProtocol.YOP_ENCRYPT_PROTOCOL_V1_REQ;
import static com.yeepay.yop.sdk.constants.CharacterConstants.*;
import static com.yeepay.yop.sdk.http.Headers.YOP_ENCRYPT;
import static com.yeepay.yop.sdk.utils.HttpUtils.isJsonContentType;
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
     * @param provider             服务方
     * @param env                  环境
     * @param request              请求
     * @param appKey               应用
     * @param encryptor            加密器
     * @param encryptOptionsFuture 加密选项
     */
    public static boolean encrypt(String provider, String env, Request<? extends BaseRequest> request, String appKey, YopEncryptor encryptor, Future<EncryptOptions> encryptOptionsFuture)
            throws UnsupportedEncodingException {
        YopRequestConfig requestConfig = request.getOriginalRequestObject().getRequestConfig();
        // 商户强制不加密
        if (BooleanUtils.isFalse(requestConfig.getNeedEncrypt())) {
            LOGGER.info("request not encrypted for requestConfig needEncrypt:false");
            return false;
        }

        Set<String> encryptHeaders = Collections.emptySet();
        Set<String> encryptParams = Collections.emptySet();
        EncryptOptions encryptOptions = null;
        if (BooleanUtils.isTrue(requestConfig.getNeedEncrypt())) {
            try {
                encryptOptions = encryptOptionsFuture.get();
            } catch (Exception e) {
                LOGGER.warn("request not encrypted, EncryptOptions InitFail, ex:", e);
                EncryptOptionsCache.invalidateEncryptOptions(provider, env, appKey, requestConfig.getEncryptAlg(), requestConfig.getServerRoot());
                return false;
            }
            encryptHeaders = encryptHeaders(encryptor, requestConfig.getEncryptHeaders(), request, encryptOptions);
            encryptParams = encryptParams(encryptor, requestConfig.getEncryptParams(), request, requestConfig, encryptOptions);
        }

        // 请求、响应参数均不加密时，不传加密头
        // TODO 支持商户配置响应是否加密，并传给网关
        if (null == encryptOptions || (CollectionUtils.isEmpty(encryptHeaders) && CollectionUtils.isEmpty(encryptParams))) {
            LOGGER.info("request not encrypted for requestConfig headers:{}, params:{}",
                    requestConfig.getEncryptHeaders(), requestConfig.getEncryptParams());
            return false;
        }
        buildEncryptHeader(request, encryptHeaders, encryptParams, encryptOptions);
        return true;
    }


    /**
     * 加密协议头
     *
     * @see YopEncryptProtocol#YOP_ENCRYPT_PROTOCOL_V1_REQ
     */
    public static String buildEncryptHeader(Request<? extends BaseRequest> request, Set<String> encryptHeaders,
                                            Set<String> encryptParams, EncryptOptions encryptOptions) throws UnsupportedEncodingException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("request encrypted, requestId:{}, headers:{}, params:{}", request.getRequestId(), encryptHeaders, encryptParams);
        }

        String encryptHeader = YOP_ENCRYPT_PROTOCOL_V1_REQ.getProtocolPrefix() + SLASH +
                getPlatformCertSerialNo(encryptOptions) + SLASH +
                StringUtils.replace(encryptOptions.getAlg(), SLASH, UNDER_LINE) + SLASH +
                encryptOptions.getEncryptedCredentials() + SLASH +
                getIvAAD(encryptOptions) + SLASH +
                encryptOptions.getBigParamEncryptMode() + SLASH +
                StringUtils.join(encryptHeaders, SEMICOLON) + SLASH +
                Encodes.encodeUrlSafeBase64(StringUtils.join(encryptParams, SEMICOLON).getBytes(YopConstants.DEFAULT_ENCODING));
        LOGGER.debug("encryptHeader:{}", encryptHeader);

        // 添加加密头
        request.addHeader(YOP_ENCRYPT, encryptHeader);
        return encryptHeader;
    }

    private static String getPlatformCertSerialNo(EncryptOptions encryptOptions) {
        String platformSerialNo = (String) encryptOptions.getEnhancerInfo().get(YOP_ENCRYPT_OPTIONS_YOP_PLATFORM_CERT_SERIAL_NO);
        if (StringUtils.isBlank(platformSerialNo) || StringUtils.equalsIgnoreCase(platformSerialNo, NULL_STRING)) {
            platformSerialNo = EMPTY;
        }
        return platformSerialNo;
    }

    private static String getIvAAD(EncryptOptions encryptOptions) {
        String iv = encryptOptions.getIv(), aad = encryptOptions.getAad();
        if (StringUtils.isBlank(iv) && StringUtils.isBlank(aad)) {
            return EMPTY;
        }
        if (StringUtils.isBlank(iv)) {
            iv = EMPTY;
        }
        if (StringUtils.isBlank(aad)) {
            aad = EMPTY;
        }
        return iv + SEMICOLON + aad;
    }

    private static Set<String> encryptHeaders(YopEncryptor encryptor, Set<String> encryptHeaders,
                                              Request<? extends BaseRequest> request, EncryptOptions encryptOptions) {
        if (CollectionUtils.isEmpty(encryptHeaders)) {
            return encryptHeaders;
        }
        Set<String> finalEncryptHeaders = Sets.newHashSetWithExpectedSize(encryptHeaders.size());
        Map<String, String> headers = request.getHeaders();
        headers.forEach((k, v) -> {
            if (encryptHeaders.contains(k) && StringUtils.isNotBlank(v)) {
                headers.put(k, encryptor.encryptToBase64(v, encryptOptions));
                finalEncryptHeaders.add(k);
            }
        });
        return finalEncryptHeaders;
    }

    private static Set<String> encryptParams(YopEncryptor encryptor, Set<String> encryptParams, Request<? extends BaseRequest> request,
                                             YopRequestConfig requestConfig, EncryptOptions encryptOptions) {

        boolean totalEncrypt = BooleanUtils.isTrue(requestConfig.getTotalEncrypt());
        if (!totalEncrypt && CollectionUtils.isEmpty(encryptParams) && null == request.getContent()) {
            return encryptParams;
        }

        Set<String> finalEncryptParams = Sets.newHashSetWithExpectedSize(encryptParams.size());
        Map<String, List<String>> parameters = request.getParameters();
        encryptSimpleParams(encryptor, finalEncryptParams, encryptParams, parameters, encryptOptions, totalEncrypt);

        Map<String, List<MultiPartFile>> multiPartFiles = request.getMultiPartFiles();
        encryptMultiPartParams(encryptor, finalEncryptParams, encryptParams, multiPartFiles, encryptOptions, totalEncrypt);

        encryptContent(encryptor, finalEncryptParams, request, requestConfig, encryptOptions);
        LOGGER.debug("encryptParams finished, totalEncrypt:{}, params:{}", totalEncrypt, finalEncryptParams);
        return totalEncrypt ? YopConstants.TOTAL_ENCRYPT_PARAMS : finalEncryptParams;
    }

    private static void encryptContent(YopEncryptor encryptor, Set<String> finalEncryptParams, Request<? extends BaseRequest> request,
                                       YopRequestConfig requestConfig, EncryptOptions encryptOptions) {
        if (null == request.getContent()) return;

        if (isJsonContentType(request)) {
            byte[] jsonBytes = encryptJsonParams(encryptor, finalEncryptParams, requestConfig, request.getContent(), encryptOptions);
            RestartableInputStream restartableInputStream = RestartableInputStream.wrap(jsonBytes);
            request.setContent(restartableInputStream);
            // ！！！覆盖掉原文计算的length，否则httpClient会用原文指定的length头来发送报文，导致完整性校验不通过
            request.addHeader(Headers.CONTENT_LENGTH, String.valueOf(jsonBytes.length));
        } else if (YopContentType.OCTET_STREAM.equals(request.getContentType())) {
            request.setContent(encryptor.encrypt(request.getContent(), encryptOptions));
            finalEncryptParams.add(DOLLAR);
        } else {
            throw new YopClientException("body content is not supported, contentType:" + request.getContentType());
        }
    }

    private static byte[] encryptJsonParams(YopEncryptor encryptor, Set<String> finalEncryptParams, YopRequestConfig requestConfig,
                                            InputStream content, EncryptOptions encryptOptions) {
        try {
            String originJson = IOUtils.toString(content, YopConstants.DEFAULT_ENCODING);
            String encryptedJson;
            Set<String> encryptPaths = null;
            // 默认整体加
            boolean totalEncrypt = true;
            if (BooleanUtils.isFalse(requestConfig.getTotalEncrypt())) {
                encryptPaths = resolveAllJsonPaths(originJson, requestConfig.getEncryptParams());
                // 防止设置非法的jsonpath，再次校验参数
                totalEncrypt = JsonUtils.isTotalEncrypt(encryptPaths);
            }

            if (!totalEncrypt) {
                DocumentContext valReadWriteCtx = JsonPath.parse(originJson);
                for (String encryptPath : encryptPaths) {
                    try {
                        String plainVal = JsonUtils.toJsonString(valReadWriteCtx.read(encryptPath));
                        if (StringUtils.isNotBlank(plainVal)) {
                            String encrypted = encryptor.encryptToBase64(plainVal, encryptOptions);
                            valReadWriteCtx.set(encryptPath, encrypted);
                            finalEncryptParams.add(encryptPath);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("json request encrypted partly, path:{}, source:{}, target:{}, options:{}",
                                        encryptPath, plainVal, encrypted, encryptOptions);
                            }
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
            LOGGER.debug("json request encrypted, source:{}, target:{}, options:{}", originJson, encryptedJson, encryptOptions);
            return encryptedJson.getBytes(YopConstants.DEFAULT_ENCODING);
        } catch (IOException e) {
            throw new YopClientException("error happened when encrypt json", e);
        }
    }

    private static void encryptMultiPartParams(YopEncryptor encryptor, Set<String> finalEncryptParams, Set<String> encryptParams,
                                               Map<String, List<MultiPartFile>> multiPartFiles, EncryptOptions encryptOptions, boolean totalEncrypt) {
        multiPartFiles.forEach((name, list) -> {
            if (CollectionUtils.isNotEmpty(list) && (totalEncrypt || encryptParams.contains(name))) {
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

    private static void encryptSimpleParams(YopEncryptor encryptor, Set<String> finalEncryptParams, Set<String> encryptParams,
                                            Map<String, List<String>> parameters, EncryptOptions encryptOptions, boolean totalEncrypt) {
        parameters.forEach((name, list) -> {
            if (CollectionUtils.isNotEmpty(list) && (totalEncrypt || encryptParams.contains(name))) {
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
