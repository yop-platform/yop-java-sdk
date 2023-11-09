package com.yeepay.yop.sdk.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopOauth2Credentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.base.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.base.cache.EncryptOptionsCache;
import com.yeepay.yop.sdk.base.cache.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.client.router.GateWayRouter;
import com.yeepay.yop.sdk.client.router.ServerRootSpace;
import com.yeepay.yop.sdk.client.router.SimpleGateWayRouter;
import com.yeepay.yop.sdk.client.router.YopRouter;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.exception.*;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.http.YopHttpClient;
import com.yeepay.yop.sdk.http.YopHttpClientFactory;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.*;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzeResult;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import static com.yeepay.yop.sdk.internal.RequestAnalyzer.*;

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
        return new UriRouteInvokerWrapper<>(
                new YopInvoker<>(executionParams, executionContext, new SimpleExceptionAnalyzer(null != circuitBreakerConfig ?
                        circuitBreakerConfig.getExcludeExceptions() : Collections.emptySet(),
                        clientConfiguration.getRetryExceptions())),
                SimpleUriRetryPolicy.singleton(),
                new YopRouter<>(gateWayRouter)).invoke();
    }

    private interface YopCircuitBreaker {

        <Input extends BaseRequest, Output extends BaseResponse> Output execute(Request<Input> request,
                                                                                UriRouteInvoker<ClientExecutionParams<Input, Output>, Output,
                                                                                        ExecutionContext, ExceptionAnalyzeResult> invoker);

    }

    private class YopSentinelCircuitBreaker implements YopCircuitBreaker {

        public YopSentinelCircuitBreaker(ServerRootSpace serverRootSpace, YopCircuitBreakerConfig circuitBreakerConfig) {
            final ArrayList<URI> serverRoots = Lists.newArrayList(serverRootSpace.getYosServerRoot(),
                    serverRootSpace.getSandboxServerRoot());
            if (CollectionUtils.isNotEmpty(serverRootSpace.getPreferredEndPoint())) {
                serverRoots.addAll(serverRootSpace.getPreferredEndPoint());
            }
            if (CollectionUtils.isNotEmpty(serverRootSpace.getPreferredYosEndPoint())) {
                serverRoots.addAll(serverRootSpace.getPreferredYosEndPoint());
            }
            YopDegradeRuleHelper.initDegradeRule(serverRoots, circuitBreakerConfig);
        }

        @Override
        public <Input extends BaseRequest, Output extends BaseResponse> Output execute(Request<Input> request,
                                                                                       UriRouteInvoker<ClientExecutionParams<Input, Output>, Output,
                                                                                               ExecutionContext, ExceptionAnalyzeResult> invoker)
                throws YopClientException, YopHttpException, YopUnknownException, YopHostException {

            Entry entry = null;
            try {
                final URI serverRoot = request.getEndpoint();
                final String host = serverRoot.toString();
                YopDegradeRuleHelper.addDegradeRule(serverRoot, circuitBreakerConfig);
                entry = SphU.entry(host);
                return doExecute(request, invoker);
            } catch (YopClientException | YopHttpException | YopUnknownException ex) {
                throw ex;
            } catch (Throwable ex) {
                if (BlockException.isBlockException(ex)) {
                    final YopHostBlockException hostBlockException = new YopHostBlockException("ServerRoot Blocked, ex:", ex);
                    invoker.addException(invoker.getExceptionAnalyzer().analyze(hostBlockException));
                    throw hostBlockException;
                }
                throw new YopUnknownException("UnExpected Error, ", ex);
            } finally {
                if (null != entry) {
                    final ExceptionAnalyzeResult lastException = invoker.getLastException();
                    if (lastException.isNeedDegrade()) {
                        Tracer.trace(lastException.getException());
                    }
                    entry.exit();
                }
            }
        }
    }

    private <Input extends BaseRequest, Output extends BaseResponse> Output doExecute(Request<Input> request,
                                                                                      UriRouteInvoker<ClientExecutionParams<Input, Output>, Output,
                                                                                              ExecutionContext, ExceptionAnalyzeResult> invoker)
            throws YopClientException, YopHttpException, YopUnknownException {

        final long start = System.currentTimeMillis();
        URI serverRoot = request.getEndpoint();
        Throwable throwable = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Begin HttpInvoke, value:{}", serverRoot);
            }
            return client.execute(request, request.getOriginalRequestObject().getRequestConfig(),
                    invoker.getContext(), invoker.getInput().getResponseHandler());
        } catch (YopClientException clientError) {//客户端异常&业务异常
            throwable = clientError;
            throw clientError;
        } catch (YopHttpException httpException) {//HTTP调用异常
            throwable = httpException;
            throw httpException;
        } catch (Throwable ex) {// 非预期异常
            throwable = ex;
            throw new YopUnknownException("UnExpected Error, ", ex);
        } finally {
            String result = "success";
            if (null != throwable) {
                final ExceptionAnalyzeResult analyzeResult = invoker.getExceptionAnalyzer().analyze(throwable);
                invoker.addException(analyzeResult);
                result = "fail caused by " + analyzeResult.getExDetail();
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Finish HttpInvoke, value:{}, result:{}, elapsed:{}.",
                        serverRoot, result, System.currentTimeMillis() - start);
            }
        }
    }

    private class YopInvoker<Input extends BaseRequest, Output extends BaseResponse>
            implements UriRouteInvoker<ClientExecutionParams<Input, Output>, Output,
            ExecutionContext, ExceptionAnalyzeResult> {

        private URI serverRoot;
        private ClientExecutionParams<Input, Output> executionParams;
        private ExecutionContext executionContext;

        private boolean enableCircuitBreaker = false;

        private List<ExceptionAnalyzeResult> exceptions = new LinkedList<>();

        private ExceptionAnalyzeResult lastException;

        private ExceptionAnalyzer<ExceptionAnalyzeResult> exceptionAnalyzer;

        public YopInvoker(ClientExecutionParams<Input, Output> executionParams,
                          ExecutionContext executionContext, ExceptionAnalyzer<ExceptionAnalyzeResult> exceptionAnalyzer) {
            this.executionParams = executionParams;
            this.executionContext = executionContext;
            this.exceptionAnalyzer = exceptionAnalyzer;
        }

        @Override
        public Output invoke() {
            // 准备http参数
            Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
            request.setEndpoint(serverRoot);

            // 发起http调用
            if (enableCircuitBreaker && null != circuitBreaker) {
                return circuitBreaker.execute(request, this);
            } else {
                return doExecute(request, this);
            }
        }

        @Override
        public ClientExecutionParams<Input, Output> getInput() {
            return executionParams;
        }

        @Override
        public void setInput(ClientExecutionParams<Input, Output> executionParams) {
            this.executionParams = executionParams;
        }

        @Override
        public URI getUri() {
            return serverRoot;
        }

        @Override
        public void setUri(URI uri) {
            this.serverRoot = uri;
        }

        @Override
        public void setContext(ExecutionContext context) {
            this.executionContext = context;
        }

        @Override
        public ExecutionContext getContext() {
            return executionContext;
        }

        @Override
        public boolean isCircuitBreakerEnable() {
            return enableCircuitBreaker;
        }

        @Override
        public void enableCircuitBreaker() {
            this.enableCircuitBreaker = true;
        }

        @Override
        public void disableCircuitBreaker() {
            this.enableCircuitBreaker = false;
        }

        @Override
        public List<ExceptionAnalyzeResult> getExceptions() {
            return exceptions;
        }

        @Override
        public void addException(ExceptionAnalyzeResult exception) {
            this.exceptions.add(exception);
            this.lastException = exception;
        }

        @Override
        public ExceptionAnalyzeResult getLastException() {
            return this.lastException;
        }

        @Override
        public ExceptionAnalyzer<ExceptionAnalyzeResult> getExceptionAnalyzer() {
            return exceptionAnalyzer;
        }

        @Override
        public void setExceptionAnalyzer(ExceptionAnalyzer<ExceptionAnalyzeResult> analyzer) {
            this.exceptionAnalyzer = analyzer;
        }
    }

    private <Output extends BaseResponse, Input extends BaseRequest> ExecutionContext getExecutionContext(
            ClientExecutionParams<Input, Output> executionParams) {
        AuthorizationReq authorizationReq = getAuthorizationReq(executionParams.getInput());
        if (authorizationReq == null) {
            throw new YopClientException("no authenticate req defined");
        } else {
            YopRequestConfig requestConfig = executionParams.getInput().getRequestConfig();
            YopCredentials<?> credential = getCredentials(requestConfig, authorizationReq);
            YopEncryptor encryptor = null;
            Future<EncryptOptions> encryptOptions = null;
            if (isEncryptSupported(credential, requestConfig)) {
                encryptor = getEncryptor(requestConfig);
                encryptOptions = EncryptOptionsCache.loadEncryptOptions(credential.getAppKey(),
                        requestConfig.getEncryptAlg(), requestConfig.getServerRoot());
            }
            ExecutionContext.Builder builder = ExecutionContext.Builder.anExecutionContext()
                    .withYopCredentials(credential)
                    .withEncryptor(encryptor)
                    .withEncryptOptions(encryptOptions)
                    .withSigner(YopSignerFactory.getSigner(authorizationReq.getSignerType()))
                    .withSignOptions(authorizationReq.getSignOptions());
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

        // 根据商户配置的密钥识别可用的安全需求
        List<CertTypeEnum> availableCerts = checkAvailableCerts(customAppKey);
        List<AuthorizationReq> authReqsForApi = checkAuthReqsByApi(input.getOperationId());
        return computeSecurityReq(availableCerts, authReqsForApi);
    }

    private AuthorizationReq computeSecurityReq(List<CertTypeEnum> availableCerts, List<AuthorizationReq> authReqsForApi) {
        for (AuthorizationReq authorizationReq : authReqsForApi) {
            if (availableCerts.contains(CertTypeEnum.parse(authorizationReq.getCredentialType()))) {
                return authorizationReq;
            }
        }
        throw new YopClientException("can not computeSecurityReq, please check your cert config");
    }

    private List<AuthorizationReq> checkAuthReqsByApi(String operationId) {
        List<AuthorizationReq> apiAuthReqs = authorizationReqRegistry.getAuthorizationReq(operationId);
        if (CollectionUtils.isEmpty(apiAuthReqs)) {
            // api未配置，不太可能吧，留个默认值
            return AuthorizationReqSupport.getDefaultAuthReqsForApi();
        }
        return apiAuthReqs;
    }

    private AuthorizationReq checkCustomSecurityReq(String customSecurityReq) {
        AuthorizationReq authorizationReq = AuthorizationReqSupport.getAuthorizationReq(customSecurityReq);
        if (authorizationReq == null) {
            throw new YopClientException("unsupported customSecurityReq:" + customSecurityReq);
        }
        return authorizationReq;
    }

    private List<CertTypeEnum> checkAvailableCerts(String appKey) {
        List<CertTypeEnum> configPrivateCerts = yopCredentialsProvider.getSupportCertTypes(appKey);
        if (CollectionUtils.isEmpty(configPrivateCerts)) {
            throw new YopClientException("can not find private key for appKey:" + appKey);
        }
        return configPrivateCerts;
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}
