package com.yeepay.yop.sdk.client;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.google.common.collect.Lists;
import com.netflix.hystrix.*;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.base.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.base.cache.EncryptOptionsCache;
import com.yeepay.yop.sdk.base.cache.YopDegradeRuleWrapper;
import com.yeepay.yop.sdk.client.router.GateWayRouter;
import com.yeepay.yop.sdk.client.router.RouteUtils;
import com.yeepay.yop.sdk.client.router.ServerRootSpace;
import com.yeepay.yop.sdk.client.router.SimpleGateWayRouter;
import com.yeepay.yop.sdk.config.provider.file.YopHystrixConfig;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopHostException;
import com.yeepay.yop.sdk.exception.YopHttpException;
import com.yeepay.yop.sdk.exception.YopUnknownException;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.http.HttpResponseHandler;
import com.yeepay.yop.sdk.http.YopHttpClient;
import com.yeepay.yop.sdk.http.YopHttpClientFactory;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static com.yeepay.yop.sdk.YopConstants.DEFAULT_YOP_CIRCUIT_BREAKER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;
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
        final YopHystrixConfig hystrixConfig = this.clientConfiguration.getHystrixConfig();
        if (DEFAULT_YOP_CIRCUIT_BREAKER.equalsIgnoreCase(hystrixConfig.getCircuitBreaker())) {
            this.circuitBreaker = new YopSentinelCircuitBreaker(serverRootSpace, hystrixConfig);
        } else {
            this.circuitBreaker = new YopHystrixCircuitBreaker();
        }
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
        List<URI> endPoints = gateWayRouter.routes(executionContext.getYopCredentials().getAppKey(), request);
        return executeWithRetry(executionParams, executionContext, endPoints);
    }

    private <Input extends BaseRequest, Output extends BaseResponse> Output executeWithRetry(ClientExecutionParams<Input, Output> executionParams,
                                                                                             ExecutionContext executionContext, List<URI> endPoints) {
        int retryCount = 0;
        Exception lastError = null;
        for (URI endPoint : endPoints) {
            if (retryCount++ > clientConfiguration.getMaxRetryCount()) {
                throw new YopClientException("MaxRetryCount Hit, value:" + clientConfiguration.getMaxRetryCount() + ",lastEx:", lastError);
            }
            try {
                return circuitBreaker.execute(endPoint, executionParams, executionContext);
            } catch (YopHostException hostError) {//域名异常
                lastError = hostError;
            } /*catch (Exception otherError) {客户端异常、业务异常、其他未知异常，交给上层处理}*/
        }

        // 再尝试一次
        LOGGER.warn("All Hosts Not Available, value:{}, And Will Try One More Time Randomly",  endPoints);
        Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
        request.setEndpoint(RouteUtils.randomOne(endPoints));
        return doExecute(request, executionContext, executionParams.getResponseHandler());
    }

    private interface YopCircuitBreaker {

        <Input extends BaseRequest, Output extends BaseResponse> Output execute(URI endPoint,
                                                                                ClientExecutionParams<Input, Output> executionParams,
                                                                                ExecutionContext executionContext);
    }

    private class YopHystrixCircuitBreaker implements YopCircuitBreaker {

        @Override
        public <Input extends BaseRequest, Output extends BaseResponse> Output execute(URI endPoint,
                                                                                               ClientExecutionParams<Input, Output> executionParams,
                                                                                               ExecutionContext executionContext) {
            Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
            request.setEndpoint(endPoint);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying Host, value:{}", endPoint);
            }

            try {
                return new ClientExecuteCommand<Input, Output>(
                        configToSetter(clientConfiguration.getHystrixConfig(), StringUtils.substringBefore(endPoint.toString(), "?")),
                        executionContext, request, executionParams.getResponseHandler()).execute();
            } catch (HystrixBadRequestException e) {// 客户端异常
                LOGGER.error("Client Error, ex:", e);
                if (e.getCause() instanceof YopClientException) {
                    throw (YopClientException) e.getCause();
                }
                throw new YopClientException("Client Error, ex:", e);
            } catch (HystrixRuntimeException e) {// Hystrix异常
                switch (e.getFailureType()) {
                    // 当笔切换，重试
                    case SHORTCIRCUIT:
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Host ShortCircuit, value:{}", endPoint);
                        }
                        throw new YopHostException("Host ShortCircuit, ex:", e);
                    case REJECTED_THREAD_EXECUTION:
                    case REJECTED_SEMAPHORE_FALLBACK:
                    case REJECTED_SEMAPHORE_EXECUTION:
                        // 理论上不会到这里
                        throw new YopClientException("Host CommandRejected, value:" + endPoint, e);
                    case BAD_REQUEST_EXCEPTION:
                        throw new YopClientException(ExceptionUtils.getMessage(e), ExceptionUtils.getRootCause(e));
                    case COMMAND_EXCEPTION:
                        if (e.getCause() instanceof YopHostException) {
                            throw (YopHostException) e.getCause();
                        }
                        if (e.getCause() instanceof YopHttpException) {
                            throw (YopHttpException) e.getCause();
                        }
                    default: // 超时、或其他未知异常不再重试
                        handleUnExpectedError(e);
                }
            } catch (Exception e) {// 其他异常
                handleUnExpectedError(e);
            }
            return null;
        }
    }

    private class YopSentinelCircuitBreaker implements YopCircuitBreaker {

        public YopSentinelCircuitBreaker(ServerRootSpace serverRootSpace, YopHystrixConfig hystrixConfig) {
            final ArrayList<URI> serverRoots = Lists.newArrayList(serverRootSpace.getServerRoot(),
                    serverRootSpace.getYosServerRoot(), serverRootSpace.getSandboxServerRoot());
            if (CollectionUtils.isNotEmpty(serverRootSpace.getPreferredEndPoint())) {
                serverRoots.addAll(serverRootSpace.getPreferredEndPoint());
            }
            if (CollectionUtils.isNotEmpty(serverRootSpace.getPreferredYosEndPoint())) {
                serverRoots.addAll(serverRootSpace.getPreferredYosEndPoint());
            }
            YopDegradeRuleWrapper.initDegradeRule(serverRoots, hystrixConfig);
        }

        @Override
        public <Input extends BaseRequest, Output extends BaseResponse> Output execute(URI endPoint,
                                                                                       ClientExecutionParams<Input, Output> executionParams,
                                                                                       ExecutionContext executionContext) {
            Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
            request.setEndpoint(endPoint);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying Host, value:{}", endPoint);
            }
            final String host = StringUtils.substringBefore(endPoint.toString(), "?");

            Entry entry = null;
            Throwable serverError = null;
            try {
                entry = SphU.entry(host);
                return doExecute(request, executionContext, executionParams.getResponseHandler());
            } catch (YopClientException clientError) {//客户端异常&业务异常
                throw clientError;
            } catch (YopHttpException serverEx) {// 调用YOP异常
                final AnalyzeException analyzedEx = AnalyzeException.analyze(serverEx, clientConfiguration);
                if (analyzedEx.isServerError()) {
                    serverError = serverEx;
                }
                if (analyzedEx.isNeedRetry()) {//域名异常
                    throw new YopHostException("Need Change Host, ex:", serverEx);
                }
                throw new YopClientException("Client Error, ex:", serverEx);
            } catch (Exception ex) {//熔断异常&未知异常
                if (BlockException.isBlockException(ex)) {
                    throw new YopHostException("Need Change Host, ex:", ex);
                } else {
                    serverError = ex;
                    handleUnExpectedError(ex);
                }
            } finally {
                if (entry != null) {
                    if (null != serverError) {
                        Tracer.trace(serverError);
                    }
                    entry.exit();
                }
            }
            throw new YopUnknownException("UnExpected Situation, Cant Be Here.");
        }
    }

    private void handleUnExpectedError(Exception ex) {
        LOGGER.error("UnExpected Error, ex:", ex);
        throw new YopUnknownException("UnExpected Error, " + ExceptionUtils.getMessage(ex), ExceptionUtils.getRootCause(ex));
    }

    public class ClientExecuteCommand<Input extends BaseRequest, Output extends BaseResponse> extends HystrixCommand<Output> {

        private ExecutionContext executionContext;
        private Request<Input> request;
        private HttpResponseHandler<Output> responseHandler;

        public ClientExecuteCommand(Setter setter, ExecutionContext executionContext,
                                    Request<Input> request, HttpResponseHandler<Output> responseHandler) {
            super(setter);
            this.executionContext = executionContext;
            this.request = request;
            this.responseHandler = responseHandler;
        }

        @Override
        protected Output run() throws Exception {
            try {
                return doExecute(request, executionContext, responseHandler);
            } catch (YopHttpException e) {
                final AnalyzeException analyzedEx = AnalyzeException.analyze(e, clientConfiguration);
                if (analyzedEx.isNeedRetry()) {
                    throw new YopHostException("Need Change Host, ", e);
                }
                if (analyzedEx.isServerError()) {
                    throw e;
                } else {
                    throw new HystrixBadRequestException(getCommandKey() + " Fail, ", e);
                }
            } catch (YopClientException e) {
                // 不计入短路
                throw new HystrixBadRequestException(getCommandKey() + " Fail, ", e);
            }
        }

    }

    private static class AnalyzeException {

        private boolean needRetry;
        private boolean serverError = true;

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

        public static AnalyzeException analyze(Throwable e, ClientConfiguration clientConfiguration) {
            final AnalyzeException result = new AnalyzeException();
            final Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (null == rootCause) {
                return result;
            }

            // 当笔重试 (域名异常)
            final String exType = rootCause.getClass().getCanonicalName(), exMsg = rootCause.getMessage();
            final List<String> curException = Lists.newArrayList(exType, exType + COLON + exMsg);

            if (CollectionUtils.containsAny(clientConfiguration.getRetryExceptions(), curException)) {
                result.setNeedRetry(true);
                return result;
            }

            // 不重试，不计入短路
            if (CollectionUtils.containsAny(clientConfiguration.getHystrixConfig().getExcludeExceptions(), curException)) {
                result.setServerError(false);
                return result;
            }

            // 其他异常，计入短路
            return result;
        }
    }

    private <Output extends BaseResponse, Input extends BaseRequest> Output doExecute(Request<Input> request, ExecutionContext executionContext, HttpResponseHandler<Output> responseHandler) {
        return client.execute(request, request.getOriginalRequestObject().getRequestConfig(),
                executionContext, responseHandler);
    }

    private HystrixCommand.Setter configToSetter(YopHystrixConfig config, String commandKey) {
        return HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(config.getGroupKey()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey))
                .andCommandPropertiesDefaults(HystrixCommandProperties.defaultSetter()
                        .withCircuitBreakerEnabled(config.isCircuitBreakerEnabled())
                        .withCircuitBreakerRequestVolumeThreshold(config.getCircuitBreakerRequestVolumeThreshold())
                        .withCircuitBreakerErrorThresholdPercentage(config.getCircuitBreakerErrorThresholdPercentage())
                        .withCircuitBreakerSleepWindowInMilliseconds(config.getCircuitBreakerSleepWindowInMilliseconds())
                        .withCircuitBreakerForceClosed(config.isCircuitBreakerForceClosed())
                        .withCircuitBreakerForceOpen(config.isCircuitBreakerForceOpen())
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.valueOf(config.getExecutionIsolationStrategy()))
                        .withExecutionTimeoutEnabled(config.isExecutionTimeoutEnabled())
                        .withExecutionTimeoutInMilliseconds(config.getExecutionIsolationThreadTimeoutInMilliseconds())
                        .withExecutionIsolationSemaphoreMaxConcurrentRequests(config.getExecutionIsolationSemaphoreMaxConcurrentRequests())
                        .withExecutionIsolationThreadInterruptOnFutureCancel(config.isExecutionIsolationThreadInterruptOnCancel())
                        .withExecutionIsolationThreadInterruptOnTimeout(config.isExecutionIsolationThreadInterruptOnTimeout())
                        .withFallbackEnabled(false)
                        .withMetricsRollingStatisticalWindowInMilliseconds(config.getCbMetricsRollingStatsTimeInMilliseconds())
                        .withMetricsRollingStatisticalWindowBuckets(config.getCbMetricsRollingStatsNumBuckets())
                        .withMetricsRollingPercentileEnabled(config.isCbMetricsRollingPercentileEnabled())
                        .withMetricsRollingPercentileWindowInMilliseconds(config.getCbMetricsRollingPercentileTimeInMilliseconds())
                        .withMetricsRollingPercentileWindowBuckets(config.getCbMetricsRollingPercentileNumBuckets())
                        .withMetricsRollingPercentileBucketSize(config.getCbMetricsRollingPercentileBucketSize())
                        .withMetricsHealthSnapshotIntervalInMilliseconds(config.getCbMetricsHealthSnapshotIntervalInMilliseconds()))
                .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(config.getThreadPoolKey()))
                .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.defaultSetter()
                        .withCoreSize(config.getCoreSize())
                        .withMaximumSize(config.getMaximumSize())
                        .withMaxQueueSize(config.getMaxQueueSize())
                        .withAllowMaximumSizeToDivergeFromCoreSize(config.isAllowMaximumSizeToDivergeFromCoreSize())
                        .withQueueSizeRejectionThreshold(config.getQueueSizeRejectionThreshold())
                        .withKeepAliveTimeMinutes(config.getKeepAliveTimeMinutes())
                        .withMetricsRollingStatisticalWindowInMilliseconds(config.getTpMetricsRollingStatsTimeInMilliseconds())
                        .withMetricsRollingStatisticalWindowBuckets(config.getTpMetricsRollingStatsNumBuckets())
                );
    }

    private <Output extends BaseResponse, Input extends BaseRequest> ExecutionContext getExecutionContext(
            ClientExecutionParams<Input, Output> executionParams) {
        AuthorizationReq authorizationReq = getAuthorizationReq(executionParams.getInput());
        if (authorizationReq == null) {
            throw new YopClientException("no authenticate req defined");
        } else {
            YopRequestConfig requestConfig = executionParams.getInput().getRequestConfig();
            YopCredentials<?> credential = getCredentials(requestConfig, authorizationReq);
            // 仅国密请求支持加密
            YopEncryptor encryptor = null;
            Future<EncryptOptions> encryptOptions = null;
            if (isEncryptSupported(credential, requestConfig)) {
                encryptor = getEncryptor(requestConfig);
                encryptOptions = EncryptOptionsCache.loadEncryptOptions(credential.getAppKey(), requestConfig.getEncryptAlg());
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
        String appKey = input.getRequestConfig().getAppKey();
        // 获取商户自定义的安全需求
        String customSecurityReq = input.getRequestConfig() == null ? null : input.getRequestConfig().getSecurityReq();
        if (StringUtils.isNotEmpty(customSecurityReq)) {
            return checkCustomSecurityReq(customSecurityReq);
        }

        // 根据商户配置的密钥识别可用的安全需求
        List<CertTypeEnum> availableCerts = checkAvailableCerts(appKey);
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
