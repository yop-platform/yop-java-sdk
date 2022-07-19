/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common.callback.protocol;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequest;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * title: Yop回调协议工厂<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/16
 */
public class YopCallbackProtocolFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCallbackProtocolFactory.class);

    public static YopCallbackProtocol fromRequest(YopCallbackRequest request) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("begin to build YopCallbackProtocol, request:{}", request);
        }
        final Map<String, String> headers = request.getCanonicalHeaders();
        if (MapUtils.isNotEmpty(headers)) {
            String authorization = headers.get(Headers.AUTHORIZATION.toLowerCase());
            // 目前只有国密有此认证头
            if (StringUtils.isNotBlank(authorization) && authorization.startsWith(YopConstants.SM2_PROTOCOL_PREFIX)) {
                return new YopSm2CallbackProtocol(request);
            }
        }

        return deprecatedProtocol(request);
    }

    @Deprecated
    private static YopCallbackProtocol deprecatedProtocol(YopCallbackRequest request) {
        Map<String, String> params;
        switch (request.getContentType()) {
            case JSON:
                String json = (String) request.getContent();
                params = JsonUtils.fromJsonString(json, Map.class);
                break;
            case FORM_URL_ENCODE:
                params = toParams(request.getParams());
                break;
            default:
                throw new YopClientException("unsupported content Type for YopCallback, type:" + request.getContentType());
        }

        String appKey = params.get("customerIdentification");
        String algorithm = params.get("algorithm");
        if (StringUtils.equals(algorithm, YopConstants.SM4_CALLBACK_ALGORITHM)) {
            return new YopSm4CallbackProtocol().setCustomerIdentification(appKey)
                    .setAlgorithm(algorithm).setCertType(CertTypeEnum.SM4)
                    .setAssociatedData(params.get("associatedData")).setNonce(params.get("nonce"))
                    .setCipherText(params.get("cipherText"))
                    .setOriginRequest(request);
        } else {
            return new YopRsaCallbackProtocol()
                    .setCustomerIdentification(appKey).setResponse(params.get("response"))
                    .setOriginRequest(request);
        }
    }

    private static Map<String, String> toParams(Map<String, List<String>> params) {
        Map<String, String> result = Maps.newHashMapWithExpectedSize(params.size());
        for (Map.Entry<String, List<String>> param : params.entrySet()) {
            result.put(param.getKey(), CollectionUtils.isNotEmpty(param.getValue())
                    ? param.getValue().get(0) : null);
        }
        return result;
    }
}
