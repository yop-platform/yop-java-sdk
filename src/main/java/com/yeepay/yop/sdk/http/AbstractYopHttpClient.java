/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.http;

import com.google.common.collect.ImmutableSet;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.client.ClientConfiguration;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2021/12/6
 */
public abstract class AbstractYopHttpClient implements YopHttpClient {

    protected static final Set<HttpMethodName> PAYLOAD_SUPPORT_METHODS =
            ImmutableSet.of(HttpMethodName.POST, HttpMethodName.PUT, HttpMethodName.DELETE);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Client configuration options, such as proxy settings, max retries, etc.
     */
    protected ClientConfiguration clientConfig;

    public AbstractYopHttpClient(ClientConfiguration clientConfig) {
        checkNotNull(clientConfig, "config should not be null.");
        this.clientConfig = clientConfig;
    }

    /**
     * 签名&加密请求
     * @param request
     * @param yopRequestConfig
     * @param executionContext
     * @param <Input>
     */
    protected  <Input extends BaseRequest> void configYopRequest(Request<Input> request, YopRequestConfig yopRequestConfig, ExecutionContext executionContext) {
        // 请求标头
        YopCredentials yopCredentials = executionContext.getYopCredentials();
        setAppKey(request, yopCredentials);
        setUserAgent(request);

        // 签名
        signRequest(request, executionContext);

        // 加密
        if (BooleanUtils.isTrue(yopRequestConfig.getNeedEncrypt())) {
            encryptRequest(request, executionContext);
        }
    }

    /**
     * 加密请求
     *
     * @param request          请求
     * @param executionContext 执行上下文
     * @param <Input>          请求类
     */
    private <Input extends BaseRequest> void encryptRequest(Request<Input> request, ExecutionContext executionContext) {
        executionContext.getEncryptor().encrypt(request);
    }

    private <Input extends BaseRequest> void signRequest(Request<Input> request, ExecutionContext executionContext) {
        executionContext.getSigner().sign(request, executionContext.getYopCredentials(), executionContext.getSignOptions());
    }

    protected void setUserAgent(Request<? extends BaseRequest> request) {
        request.addHeader(Headers.USER_AGENT, this.clientConfig.getUserAgent());
    }

    protected void setAppKey(Request<? extends BaseRequest> request, YopCredentials yopCredentials) {
        request.addHeader(Headers.YOP_APPKEY, yopCredentials.getAppKey());
    }

}
