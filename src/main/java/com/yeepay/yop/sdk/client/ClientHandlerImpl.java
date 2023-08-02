package com.yeepay.yop.sdk.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.auth.cache.YopCredentialsCache;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopOauth2Credentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.client.router.GateWayRouter;
import com.yeepay.yop.sdk.client.router.ServerRootSpace;
import com.yeepay.yop.sdk.client.router.SimpleGateWayRouter;
import com.yeepay.yop.sdk.client.support.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.exception.*;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.http.HttpResponseHandler;
import com.yeepay.yop.sdk.http.YopHttpClient;
import com.yeepay.yop.sdk.http.YopHttpClientFactory;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yeepay.yop.sdk.utils.CharacterConstants.COLON;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientHandlerImpl.class);

    private final YopCredentialsProvider yopCredentialsProvider;

    private final AuthorizationReqRegistry authorizationReqRegistry;

    private final ClientConfiguration clientConfiguration;

    private final YopHttpClient client;

    private final GateWayRouter gateWayRouter;

    private final YopCircuitBreakerConfig circuitBreakerConfig;

    private final YopCircuitBreaker circuitBreaker;

    public ClientHandlerImpl(ClientHandlerParams handlerParams) {
        this.yopCredentialsProvider = handlerParams.getClientParams().getCredentialsProvider();
        this.authorizationReqRegistry = handlerParams.getClientParams().getAuthorizationReqRegistry();
        ServerRootSpace serverRootSpace = new ServerRootSpace(handlerParams.getClientParams().getEndPoint(),
                handlerParams.getClientParams().getYosEndPoint(), handlerParams.getClientParams().getPreferredEndPoint(),
                handlerParams.getClientParams().getPreferredYosEndPoint(), handlerParams.getClientParams().getSandboxEndPoint());
        this.gateWayRouter = new SimpleGateWayRouter(serverRootSpace);
        this.clientConfiguration = handlerParams.getClientParams().getClientConfiguration();
        this.client = buildHttpClient(handlerParams);
        this.circuitBreakerConfig = this.clientConfiguration.getCircuitBreakerConfig();
        this.circuitBreaker = new YopSentinelCircuitBreaker(serverRootSpace, this.circuitBreakerConfig);
    }

    private YopHttpClient buildHttpClient(ClientHandlerParams handlerParams) {
        YopHttpClient yopHttpClient;
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
        ExecutionContext executionContext = getExecutionContext(executionParams);
        Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
        URI serverRoot = gateWayRouter.route(executionContext.getYopCredentials().getAppKey(), request, Collections.emptyList());
        if (null != circuitBreakerConfig && circuitBreakerConfig.isEnable()) {
            return executeWithRetry(executionParams, executionContext, serverRoot, request);
        } else {
            request.setEndpoint(serverRoot);
            return doExecute(request, executionContext, executionParams.getResponseHandler());
        }
    }

    private <Input extends BaseRequest, Output extends BaseResponse> Output executeWithRetry(ClientExecutionParams<Input, Output> executionParams,
                                                                                             ExecutionContext executionContext, URI serverRoot,
                                                                                             Request<Input> request) {
        URI lastServerRoot = serverRoot;
        List<URI> excludeServerRoots = Lists.newArrayList();
        while (!excludeServerRoots.contains(lastServerRoot)) {
            try {
                return circuitBreaker.execute(lastServerRoot, executionParams, executionContext);
            } catch (YopHostException hostError) {//域名异常
                excludeServerRoots.add(lastServerRoot);
                lastServerRoot = gateWayRouter.route(executionContext.getYopCredentials().getAppKey(), request, excludeServerRoots);
            } /*catch (Exception otherError) {客户端异常、业务异常、其他未知异常，交给上层处理}*/
        }

        // 如果所有域名均熔断，则用最早熔断域名兜底
        lastServerRoot = gateWayRouter.route(executionContext.getYopCredentials().getAppKey(), request, excludeServerRoots);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("All ServerRoots Unavailable, Last Try, tried:{}, last:{}", excludeServerRoots, lastServerRoot);
        }
        Request<Input> marshalled = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
        marshalled.setEndpoint(lastServerRoot);
        return doExecute(marshalled, executionContext, executionParams.getResponseHandler());
    }

    private interface YopCircuitBreaker {

        <Input extends BaseRequest, Output extends BaseResponse> Output execute(URI endPoint,
                                                                                ClientExecutionParams<Input, Output> executionParams,
                                                                                ExecutionContext executionContext);
    }

    private class YopSentinelCircuitBreaker implements YopCircuitBreaker {

        public YopSentinelCircuitBreaker(ServerRootSpace serverRootSpace, YopCircuitBreakerConfig circuitBreakerConfig) {
            final ArrayList<URI> serverRoots = Lists.newArrayList(serverRootSpace.getYosServerRoot(), serverRootSpace.getSandboxServerRoot());
            if (CollectionUtils.isNotEmpty(serverRootSpace.getPreferredEndPoint())) {
                serverRoots.addAll(serverRootSpace.getPreferredEndPoint());
            }
            if (CollectionUtils.isNotEmpty(serverRootSpace.getPreferredYosEndPoint())) {
                serverRoots.addAll(serverRootSpace.getPreferredYosEndPoint());
            }
            YopDegradeRuleHelper.initDegradeRule(serverRoots, circuitBreakerConfig);
        }

        @Override
        public <Input extends BaseRequest, Output extends BaseResponse> Output execute(URI serverRoot,
                                                                                       ClientExecutionParams<Input, Output> executionParams,
                                                                                       ExecutionContext executionContext) {
            Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
            request.setEndpoint(serverRoot);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying ServerRoot, value:{}", serverRoot);
            }
            final String host = serverRoot.toString();

            Entry entry = null;
            Throwable degradeError = null;
            try {
                YopDegradeRuleHelper.addDegradeRule(serverRoot, circuitBreakerConfig);
                entry = SphU.entry(host);
                return doExecute(request, executionContext, executionParams.getResponseHandler());
            } catch (YopClientException clientError) {//客户端异常&业务异常
                throw clientError;
            } catch (YopHostException | YopUnknownException serverError) {//域名异常&未知异常
                degradeError = serverError;
                throw serverError;
            } catch (Exception ex) {//熔断异常&其他未知异常
                if (BlockException.isBlockException(ex)) {
                    throw new YopHostBlockException("ServerRoot Blocked, ex:", ex);
                } else {
                    degradeError = ex;
                    handleUnExpectedError(ex);
                }
            } finally {
                if (null != entry) {
                    if (null != degradeError) {
                        Tracer.trace(degradeError);
                    }
                    entry.exit();
                }
            }
            throw new YopUnknownException("UnExpected Situation, Cant Be Here.");
        }
    }

    private void handleUnExpectedError(Exception ex) {
        throw new YopUnknownException("UnExpected Error, ", ex);
    }

    private static class AnalyzeException {

        private boolean needRetry;
        private boolean serverError = true;

        private String exDetail;

        public boolean isNeedRetry() {
            return needRetry;
        }

        public void setNeedRetry(boolean needRetry) {
            this.needRetry = needRetry;
        }

        public boolean isServerError() {
            return serverError;
        }

        public void setServerError(boolean serverError) {
            this.serverError = serverError;
        }

        public String getExDetail() {
            return exDetail;
        }

        public static AnalyzeException analyze(Throwable e, ClientConfiguration clientConfiguration) {
            final AnalyzeException result = new AnalyzeException();
            final Throwable[] allExceptions = ExceptionUtils.getThrowables(e);

            if (allExceptions.length == 1) {
                result.exDetail = e.getClass().getCanonicalName() + COLON + StringUtils.defaultString(e.getMessage());
                return result;
            }

            // 当笔重试 (域名异常)
            final List<String> exceptionDetails = Lists.newArrayList();
            for (int i = 0; i < allExceptions.length; i++) {
                Throwable rootCause = allExceptions[i];
                final String exType = rootCause.getClass().getCanonicalName(),
                        exTypeAndMsg = exType + COLON + StringUtils.defaultString(rootCause.getMessage());
                exceptionDetails.add(exType);
                exceptionDetails.add(exTypeAndMsg);
                if (clientConfiguration.getRetryExceptions().contains(exType) ||
                        clientConfiguration.getRetryExceptions().contains(exTypeAndMsg)) {
                    result.exDetail = exTypeAndMsg;
                    result.setNeedRetry(true);
                    return result;
                }
            }

            Throwable lastCause = allExceptions[allExceptions.length -1];
            result.exDetail = lastCause.getClass().getCanonicalName() + COLON + StringUtils.defaultString(lastCause.getMessage());

            // 不重试，不计入短路
            if (CollectionUtils.containsAny(clientConfiguration.getCircuitBreakerConfig().getExcludeExceptions(), exceptionDetails)) {
                result.setServerError(false);
                return result;
            }

            // 其他异常，计入短路
            return result;
        }
    }

    /**
     * 调用httpClient 并封装异常，区分客户端、业务异常、未知异常(域名异常、其他未知……)
     *
     * @param request          请求包装类
     * @param executionContext 上下文
     * @param responseHandler  响应处理器
     * @param <Output>         响应对象
     * @param <Input>          请求对象
     * @return
     */
    private <Output extends BaseResponse, Input extends BaseRequest> Output doExecute(Request<Input> request,
                                                                                      ExecutionContext executionContext,
                                                                                      HttpResponseHandler<Output> responseHandler) {
        final long start = System.currentTimeMillis();
        try {
            final Output result = client.execute(request, request.getOriginalRequestObject().getRequestConfig(),
                    executionContext, responseHandler);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Success ServerRoot, {}, elapsed:{}", request.getEndpoint(), System.currentTimeMillis() - start);
            }
            return result;
        } catch (YopClientException clientError) {//客户端异常&业务异常
            throw clientError;
        } catch (YopHttpException serverEx) {// 调用YOP异常
            final AnalyzeException analyzedEx = AnalyzeException.analyze(serverEx, clientConfiguration);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Fail ServerRoot, {}, exDetail:{}, elapsed:{}", request.getEndpoint(),
                        analyzedEx.getExDetail(), System.currentTimeMillis() - start);
            }
            if (analyzedEx.isNeedRetry()) {//域名异常
                throw new YopHostException("Need Change Host, ex:", serverEx);
            }
            if (analyzedEx.isServerError()) {
                handleUnExpectedError(serverEx);
            }
            throw new YopClientException("Client Error, ex:", serverEx);
        } catch (Exception ex) {//未知异常
            handleUnExpectedError(ex);
        }
        throw new YopUnknownException("UnExpected Situation, Cant Be Here.");
    }

    private <Output extends BaseResponse, Input extends BaseRequest> ExecutionContext getExecutionContext(
            ClientExecutionParams<Input, Output> executionParams) {
        AuthorizationReq authorizationReq = getAuthorizationReq(executionParams.getInput());
        if (authorizationReq == null) {
            throw new YopClientException("no authenticate req defined");
        } else {
            ExecutionContext.Builder builder = ExecutionContext.Builder.anExecutionContext()
                    .withSigner(YopSignerFactory.getSigner(authorizationReq.getSignerType()))
                    .withSignOptions(authorizationReq.getSignOptions());

            YopCredentials credential = executionParams.getInput().getRequestConfig().getCredentials();
            String appKey = executionParams.getInput().getRequestConfig().getAppKey();
            if (credential == null) {
                credential = yopCredentialsProvider.getCredentials(appKey, authorizationReq.getCredentialType());
            }
            if (credential == null) {
                throw new YopClientException("No credentials specified");
            }
            YopCredentialsCache.put(credential.getAppKey(), credential);
            builder.withYopCredentials(credential);
            return builder.build();
        }
    }

    private <Input extends BaseRequest> AuthorizationReq getAuthorizationReq(Input input) {
        // 获取用户自定义配置
        String customAppKey = null;
        String customSecurityReq = null;
        YopCredentials<?> customCredentials = null;
        YopRequestConfig requestConfig = input.getRequestConfig();
        if (null != requestConfig) {
            customAppKey = requestConfig.getAppKey();
            customSecurityReq = requestConfig.getSecurityReq();
            customCredentials = requestConfig.getCredentials();
        }
        if (StringUtils.isNotBlank(customSecurityReq)) {
            return checkCustomSecurityReq(customSecurityReq);
        }
        if (null != customCredentials) {
            if (customCredentials instanceof YopOauth2Credentials) {
                return AuthorizationReqSupport.getAuthorizationReq(AuthorizationReqSupport.SECURITY_OAUTH2);
            }
            final Object credential = customCredentials.getCredential();
            if (credential instanceof CredentialsItem) {
                return AuthorizationReqSupport.getAuthorizationReq(((CredentialsItem) credential).getCertType());
            }
        }

        List<CertTypeEnum> supportCertType = yopCredentialsProvider.getSupportCertTypes(customAppKey);
        List<AuthorizationReq> authorizationReqs = authorizationReqRegistry.getAuthorizationReq(input.getOperationId());
        for (AuthorizationReq authorizationReq : authorizationReqs) {
            if (supportCertType.contains(CertTypeEnum.parse(authorizationReq.getCredentialType()))) {
                return authorizationReq;
            }
        }
        throw new YopClientException("can not find private key");
    }

    private AuthorizationReq checkCustomSecurityReq(String customSecurityReq) {
        AuthorizationReq authorizationReq = AuthorizationReqSupport.getAuthorizationReq(customSecurityReq);
        if (authorizationReq == null) {
            throw new YopClientException("unsupported customSecurityReq:" + customSecurityReq);
        }
        return authorizationReq;
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}
