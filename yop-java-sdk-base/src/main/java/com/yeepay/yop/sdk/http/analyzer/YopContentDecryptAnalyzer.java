package com.yeepay.yop.sdk.http.analyzer;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.base.security.encrypt.YopEncryptProtocol;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

import static com.yeepay.yop.sdk.YopConstants.YOP_JSON_CONTENT_BIZ_KEY;
import static com.yeepay.yop.sdk.YopConstants.YOP_JSON_CONTENT_FORMAT;
import static com.yeepay.yop.sdk.utils.HttpUtils.isJsonResponse;
import static com.yeepay.yop.sdk.utils.JsonUtils.isTotalEncrypt;
import static com.yeepay.yop.sdk.utils.JsonUtils.resolveAllJsonPaths;

/**
 * title: 结果解密<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public class YopContentDecryptAnalyzer implements HttpResponseAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopContentDecryptAnalyzer.class);

    private static final YopContentDecryptAnalyzer INSTANCE = new YopContentDecryptAnalyzer();

    public static YopContentDecryptAnalyzer getInstance() {
        return INSTANCE;
    }

    private YopContentDecryptAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopResponseMetadata metadata = response.getMetadata();
        if (!context.isEncryptSupported()) return false;

        EncryptOptions reqEncryptOptions = context.getEncryptOptions();
        YopEncryptProtocol.Inst parsedEncryptProtocol = parseEncryptProtocol(metadata.getYopEncrypt(), context.getYopCredentials(), reqEncryptOptions);
        if (null == parsedEncryptProtocol) return false;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("response encrypted, requestId:{}, headers:{}, params:{}", metadata.getYopRequestId(),
                    parsedEncryptProtocol.getEncryptHeaders(), parsedEncryptProtocol.getEncryptParams());
        }
        EncryptOptions respEncryptOptions = parsedEncryptProtocol.getEncryptOptions();
        YopHttpResponse httpResponse = context.getResponse();
        decryptHeaders(httpResponse, parsedEncryptProtocol, context.getEncryptor(), respEncryptOptions);

        if (null != httpResponse.getContent()) {
            if (isJsonResponse(metadata.getContentType())) {
                decryptJsonContent(httpResponse, parsedEncryptProtocol, context.getEncryptor(), respEncryptOptions);
            } else {
                decryptDownloadStream(httpResponse, context.getEncryptor(), respEncryptOptions);
            }
        }
        return false;
    }

    private void decryptDownloadStream(YopHttpResponse httpResponse, YopEncryptor encryptor, EncryptOptions encryptOptions) {
        httpResponse.setContent(encryptor.decrypt(httpResponse.getContent(), encryptOptions));
    }

    private void decryptJsonContent(YopHttpResponse httpResponse, YopEncryptProtocol.Inst parsedEncryptProtocol,
                                    YopEncryptor encryptor, EncryptOptions encryptOptions) {
        String content = httpResponse.readContent();
        if (null == content) return;

        Map<String, Object> yopResp = JsonUtils.fromJsonString(content, Map.class);
        Object encryptBizContent = yopResp.get(YOP_JSON_CONTENT_BIZ_KEY);
        if (isTotalEncrypt(parsedEncryptProtocol.getEncryptParams())) {
            httpResponse.setContent(String.format(YOP_JSON_CONTENT_FORMAT,
                    encryptor.decryptFromBase64((String) encryptBizContent, encryptOptions)));
            return;
        }

        String jsonBizContent = JsonUtils.toJsonString(encryptBizContent);
        Set<String> encryptPaths = resolveAllJsonPaths(jsonBizContent, parsedEncryptProtocol.getEncryptParams());
        DocumentContext valReadWriteCtx = JsonPath.parse(jsonBizContent);
        for (String path : encryptPaths) {
            try {
                String encryptVal = String.valueOf(valReadWriteCtx.read(path));
                if (StringUtils.isNotBlank(encryptVal)) {
                    valReadWriteCtx.set(path, encryptor.decryptFromBase64(encryptVal, encryptOptions));
                }
            } catch (Exception e) {
                LOGGER.error("error when decrypt, path:" + path + ", json:" + jsonBizContent, e);
            }
        }
        yopResp.put(YOP_JSON_CONTENT_BIZ_KEY, valReadWriteCtx.json());
        String decryptedContent = JsonUtils.toJsonString(yopResp);
        LOGGER.debug("json request decrypted, source:{}, target:{}, options:{}", content, decryptedContent, encryptOptions);
        httpResponse.setContent(decryptedContent);
    }

    private void decryptHeaders(YopHttpResponse httpResponse, YopEncryptProtocol.Inst parsedEncryptProtocol,
                                YopEncryptor encryptor, EncryptOptions encryptOptions) {
        Map<String, String> headers = httpResponse.getHeaders();
        if (CollectionUtils.isEmpty(parsedEncryptProtocol.getEncryptHeaders()) || MapUtils.isEmpty(headers)) return;

        for (String encryptHeader : parsedEncryptProtocol.getEncryptHeaders()) {
            if (headers.containsKey(encryptHeader)) {
                headers.put(encryptHeader, encryptor.decryptFromBase64(headers.get(encryptHeader), encryptOptions));
            }
        }
    }

    private YopEncryptProtocol.Inst parseEncryptProtocol(String encryptProtocol, YopCredentials<?> yopCredentials, EncryptOptions encryptOptions) {
        if (StringUtils.isNotBlank(encryptProtocol)) {
            return YopEncryptProtocol.fromProtocol(encryptProtocol)
                    .parse(new YopEncryptProtocol.ParseParams(encryptProtocol, yopCredentials, encryptOptions));
        }
        return null;
    }
}
