package com.yeepay.yop.sdk.client;

import com.yeepay.yop.sdk.auth.cipher.DefaultEncryptor;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopPKICredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.client.router.GateWayRouter;
import com.yeepay.yop.sdk.client.router.ServerRootSpace;
import com.yeepay.yop.sdk.client.router.SimpleGateWayRouter;
import com.yeepay.yop.sdk.config.YopSdkConfig;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.http.YopHttpClient;
import com.yeepay.yop.sdk.http.YopHttpClientFactory;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.RequestConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * title: 默认客户端处理器<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 11:36
 */
public class ClientHandlerImpl implements ClientHandler {

    private final YopSdkConfigProvider yopSdkConfigProvider;

    private final YopCredentialsProvider yopCredentialsProvider;

    private final AuthorizationReqRegistry authorizationReqRegistry;

    private final YopHttpClient client;

    private final GateWayRouter gateWayRouter;

    public ClientHandlerImpl(ClientHandlerParams handlerParams) {
        this.yopSdkConfigProvider = handlerParams.getClientParams().getYopSdkConfigProvider();
        this.yopCredentialsProvider = handlerParams.getClientParams().getCredentialsProvider();
        this.authorizationReqRegistry = handlerParams.getClientParams().getAuthorizationReqRegistry();
        ServerRootSpace serverRootSpace = new ServerRootSpace(handlerParams.getClientParams().getEndPoint(),
                handlerParams.getClientParams().getYosEndPoint(), handlerParams.getClientParams().getSandboxEndPoint());
        this.gateWayRouter = new SimpleGateWayRouter(serverRootSpace);
        this.client = buildHttpClient(handlerParams);
    }

    private YopHttpClient buildHttpClient(ClientHandlerParams handlerParams) {
        YopHttpClient yopHttpClient = null;
        if (null == handlerParams) {
            yopHttpClient = YopHttpClientFactory.getDefaultClient();
        } else {
            yopHttpClient = YopHttpClientFactory.getClient(handlerParams.getClientParams().getClientConfiguration());
        }
        return yopHttpClient;
    }

    @Override
    public <Input extends BaseRequest, Output extends BaseResponse> Output execute(
            ClientExecutionParams<Input, Output> executionParams) {
        final Input input = executionParams.getInput();
        ExecutionContext executionContext = getExecutionContext(executionParams);
        Request<Input> request = executionParams.getRequestMarshaller().marshall(input);
        request.setEndpoint(gateWayRouter.route(executionContext.getYopCredentials().getAppKey(), request));
        return client.execute(request, request.getOriginalRequestObject().getRequestConfig(),
                executionContext,
                executionParams.getResponseHandler());
    }

    private <Output extends BaseResponse, Input extends BaseRequest> ExecutionContext getExecutionContext(
            ClientExecutionParams<Input, Output> executionParams) {
        AuthorizationReq authorizationReq = getAuthorizationReq(executionParams.getInput());
        if (authorizationReq == null) {
            throw new YopClientException("no authenticate req defined");
        } else {
            YopSdkConfig yopSdkConfig = yopSdkConfigProvider.getConfig();
            ExecutionContext.Builder builder = ExecutionContext.Builder.anExecutionContext()
                    .withSigner(YopSignerFactory.getSigner(authorizationReq.getSignerType()))
                    .withSignOptions(authorizationReq.getSignOptions())
                    .withYopPublicKey(yopSdkConfig.loadYopPublicKey(authorizationReq.getCredentialType()));

            YopCredentials credential = executionParams.getInput().getRequestConfig().getCredentials();
            if (credential == null) {
                String appKey = executionParams.getInput().getRequestConfig().getAppKey();
                if (StringUtils.isEmpty(appKey)) {
                    credential = yopCredentialsProvider.getCredentials("default", authorizationReq.getCredentialType());
                } else {
                    credential = yopCredentialsProvider.getCredentials(appKey, authorizationReq.getCredentialType());
                }
            }
            if (credential == null) {
                throw new YopClientException("No credentials specified");
            }
            builder.withYopCredentials(credential);

            RequestConfig requestConfig = executionParams.getInput().getRequestConfig();
            if (requestConfig != null && BooleanUtils.isTrue(requestConfig.getNeedEncrypt())) {
                if (credential instanceof YopPKICredentials) {
                    builder.withEncryptor(new DefaultEncryptor((YopPKICredentials) credential));
                } else {
                    throw new YopClientException("securityReq does't support encryption");
                }
            }
            return builder.build();
        }
    }

    private <Input extends BaseRequest> AuthorizationReq getAuthorizationReq(Input input) {
        //获取商户自定义的安全需求
        String customSecurityReq = input.getRequestConfig() == null ? null : input.getRequestConfig().getSecurityReq();
        if (StringUtils.isNotEmpty(customSecurityReq)) {
            AuthorizationReq authorizationReq = AuthorizationReqSupport.getAuthorizationReq(customSecurityReq);
            if (authorizationReq == null) {
                throw new YopClientException("unsupported customSecurityReq:" + customSecurityReq);
            }
            return authorizationReq;
        }
        return authorizationReqRegistry.getAuthorizationReq(input.getOperationId());
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}
