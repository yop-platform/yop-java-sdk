/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.service.common;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.SignOptions;
import com.yeepay.yop.sdk.auth.credentials.CertificateCredentials;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.base.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.base.auth.signer.process.YopSignProcessorFactory;
import com.yeepay.yop.sdk.base.cache.EncryptOptionsCache;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopContentType;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.service.common.callback.YopCallback;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequest;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackRequestMarshaller;
import com.yeepay.yop.sdk.service.common.callback.YopCallbackResponse;
import com.yeepay.yop.sdk.service.common.callback.handler.YopCallbackHandlerFactory;
import com.yeepay.yop.sdk.service.common.callback.protocol.YopCallbackProtocol;
import com.yeepay.yop.sdk.service.common.callback.protocol.YopCallbackProtocolFactory;
import com.yeepay.yop.sdk.service.common.callback.protocol.YopSm2CallbackProtocol;
import com.yeepay.yop.sdk.service.common.request.YopRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static com.yeepay.yop.sdk.internal.RequestAnalyzer.*;
import static com.yeepay.yop.sdk.internal.RequestEncryptor.encrypt;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;

/**
 * title: Yop商户回调处理引擎<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/12
 */
public class YopCallbackEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopCallbackEngine.class);

    /**
     * 构造Yop回调请求
     *
     * @param request  Yop请求
     * @return Yop回调请求
     */
    public static YopCallbackRequest build(YopRequest request) throws ExecutionException, InterruptedException, UnsupportedEncodingException {
        Request<YopRequest> marshalled = YopCallbackRequestMarshaller.getInstance().marshall(request);
        AuthorizationReq authorizationReq = AuthorizationReqSupport.getAuthorizationReq(request.getRequestConfig().getSecurityReq());
        if (null == authorizationReq) {
            throw new YopClientException("no authenticate req defined");
        } else {
            YopRequestConfig requestConfig = request.getRequestConfig();
            YopCredentials<?> credential = getCredentials(requestConfig, authorizationReq);
            if (isEncryptSupported(credential, requestConfig)) {
                encrypt(marshalled, getEncryptor(requestConfig), EncryptOptionsCache
                        .loadEncryptOptions(credential.getAppKey(), requestConfig.getEncryptAlg()).get());
            }
            YopSignerFactory.getSigner(authorizationReq.getSignerType()).sign(marshalled, credential, authorizationReq.getSignOptions());
        }

        final YopCallbackRequest callbackRequest = YopCallbackRequest.fromYopRequest(marshalled);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("YopCallbackRequest build:{}", callbackRequest);
        }
        return callbackRequest;
    }

    /**
     * 解析Yop回调请求
     * 1.解析协议
     * 2.解密数字信封
     *
     * @param request 原始请求
     * @return 解密后的通知参数
     */
    public static YopCallback parse(YopCallbackRequest request) {
        final YopCallback parsed = YopCallbackProtocolFactory.fromRequest(request).parse();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("YopCallbackRequest decrypted:{}", parsed);
        }
        return parsed;
    }

    /**
     * 处理Yop回调请求
     * 1.@see #parse
     * 2.查找并处理商户业务
     * 3.组装响应参数
     *
     * @param request 原始请求
     * @return Yop响应报文
     */
    public static YopCallbackResponse handle(YopCallbackRequest request) {
        final YopCallbackProtocol protocol = YopCallbackProtocolFactory.fromRequest(request);
        final YopCallback callback = protocol.parse();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("YopCallbackRequest decrypted:{}", callback);
        }
        YopCallbackResponse result;

        // 业务处理(是否可以异步？)
        try {
            YopCallbackHandlerFactory.getHandler(callback.getType()).handle(callback);
            result = YopCallbackResponse.success();
        } catch (Throwable e) {
            LOGGER.error("error when handle YopCallbackRequest, ex:", e);
            result = YopCallbackResponse.fail(e.getMessage());
        }

        // 签名
        signIfNecessary(result, protocol, callback);
        return result;
    }

    private static void signIfNecessary(YopCallbackResponse response, YopCallbackProtocol protocol, YopCallback callback) {
        try {
            if (!(protocol instanceof YopSm2CallbackProtocol)) {
                return;
            }
            response.setContentType(YopContentType.JSON);
            HashMap<String, String> headers = Maps.newHashMap();
            YopCredentials<?> credentials = YopCredentialsProviderRegistry.getProvider()
                    .getCredentials(callback.getAppKey(), CertTypeEnum.SM2.name());
            // 与网关保持一致
            final SignOptions signOptions = AuthorizationReqSupport.getAuthorizationReq("YOP-SM2-SM3")
                    .getSignOptions();
            headers.put(Headers.YOP_SIGN, YopSignProcessorFactory.getSignProcessor(CertTypeEnum.SM2.name())
                    .doSign(response.getBody().replaceAll("[ \t\n]", EMPTY),
                            (CredentialsItem) credentials.getCredential(), signOptions));
            if (credentials instanceof CertificateCredentials) {
                headers.put(Headers.YOP_SIGN_CERT_SERIAL_NO, ((CertificateCredentials) credentials).getSerialNo());
            }
            response.setHeaders(headers);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("YopCallbackResponse signed:{}", response);
            }
        } catch (Throwable e) {
            LOGGER.warn("error when sign the YopCallbackResponse, ex:", e);
        }
    }
}
