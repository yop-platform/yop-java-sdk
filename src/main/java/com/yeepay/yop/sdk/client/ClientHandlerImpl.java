package com.yeepay.yop.sdk.client;

import com.yeepay.g3.core.yop.sdk.sample.auth.*;
import com.yeepay.g3.core.yop.sdk.sample.auth.cipher.DefaultEncryptor;
import com.yeepay.g3.core.yop.sdk.sample.auth.credentials.YopRSACredentials;
import com.yeepay.g3.core.yop.sdk.sample.client.router.GateWayRouter;
import com.yeepay.g3.core.yop.sdk.sample.client.router.ServerRootSpace;
import com.yeepay.g3.core.yop.sdk.sample.client.router.SimpleGateWayRouter;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.http.ExecutionContext;
import com.yeepay.g3.core.yop.sdk.sample.http.YopHttpClient;
import com.yeepay.g3.core.yop.sdk.sample.http.YopHttpClientFactory;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseRequest;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseResponse;
import com.yeepay.g3.core.yop.sdk.sample.model.RequestConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * title: 默认客户端处理器<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2017<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 11:36
 */
public class ClientHandlerImpl implements ClientHandler {

    private final YopCredentialsProvider yopCredentialsProvider;

    private final AuthorizationReqRegistry authorizationReqRegistry;

    private final YopHttpClient client;

    private final GateWayRouter gateWayRouter;

    public ClientHandlerImpl(ClientHandlerParams handlerParams) {
        this.yopCredentialsProvider = handlerParams.getClientParams().getCredentialsProvider();
        this.authorizationReqRegistry = handlerParams.getClientParams().getAuthorizationReqRegistry();
        ServerRootSpace serverRootSpace = new ServerRootSpace(handlerParams.getClientParams().getEndPoint(),
                handlerParams.getClientParams().getYosEndPoint(), handlerParams.getClientParams().getSandboxEndPoint());
        this.gateWayRouter = new SimpleGateWayRouter(serverRootSpace);
        this.client = buildHttpClient(handlerParams);
    }

    private YopHttpClient buildHttpClient(ClientHandlerParams handlerParams) {
        return YopHttpClientFactory.getDefaultClient();
//        return new YopHttpClient(handlerParams.getClientParams().getClientConfiguration());
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
            ExecutionContext.Builder builder = ExecutionContext.Builder.anExecutionContext()
                    .withSigner(SignerSupport.getSigner(authorizationReq.getSignerType()))
                    .withSignOptions(authorizationReq.getSignOptions())
                    .withYopPublicKey(yopCredentialsProvider.getYopPublicKey(authorizationReq.getCredentialType()));
            YopCredentials credential = executionParams.getInput().getRequestConfig().getCredentials();
            if (credential == null) {
                String appKey = executionParams.getInput().getRequestConfig().getAppKey();
                if (StringUtils.isEmpty(appKey)) {
                    credential = yopCredentialsProvider.getDefaultAppCredentials(authorizationReq.getCredentialType());
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
                if (credential instanceof YopRSACredentials) {
                    builder.withEncryptor(new DefaultEncryptor((YopRSACredentials) credential));
                } else {
                    throw new YopClientException("securityReq does't support encryption");
                }
            }
            return builder.build();
        }
    }

    private <Input extends BaseRequest> AuthorizationReq getAuthorizationReq(Input input) {
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
