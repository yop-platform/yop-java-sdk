package com.yeepay.yop.sdk.client;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.CredentialsItem;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopOauth2Credentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.base.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.base.cache.EncryptOptionsCache;
import com.yeepay.yop.sdk.client.router.ServerRootSpace;
import com.yeepay.yop.sdk.client.router.YopRouter;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.config.provider.file.YopCircuitBreakerConfig;
import com.yeepay.yop.sdk.exception.*;
import com.yeepay.yop.sdk.http.ExecutionContext;
import com.yeepay.yop.sdk.http.Headers;
import com.yeepay.yop.sdk.http.YopHttpClient;
import com.yeepay.yop.sdk.http.YopHttpClientFactory;
import com.yeepay.yop.sdk.internal.Request;
import com.yeepay.yop.sdk.invoke.*;
import com.yeepay.yop.sdk.invoke.model.AnalyzedException;
import com.yeepay.yop.sdk.invoke.model.ExceptionAnalyzer;
import com.yeepay.yop.sdk.invoke.model.UriResource;
import com.yeepay.yop.sdk.model.BaseRequest;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopRequestConfig;
import com.yeepay.yop.sdk.router.config.YopRouteConfig;
import com.yeepay.yop.sdk.router.config.YopRouteConfigProvider;
import com.yeepay.yop.sdk.router.sentinel.YopDegradeRuleHelper;
import com.yeepay.yop.sdk.router.sentinel.YopSph;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Entry;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.Tracer;
import com.yeepay.yop.sdk.router.third.com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.utils.ClientUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import static com.yeepay.yop.sdk.internal.RequestAnalyzer.*;
import static com.yeepay.yop.sdk.utils.ClientUtils.isBasicClient;

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

    private final String provider;

    private final String env;

    private final YopCredentialsProvider yopCredentialsProvider;

    private final YopSdkConfigProvider yopSdkConfigProvider;

    private final YopPlatformCredentialsProvider platformCredentialsProvider;

    private final YopRouteConfigProvider routeConfigProvider;

    private final AuthorizationReqRegistry authorizationReqRegistry;

    private final YopHttpClient client;

    private final ServerRootSpace serverRootSpace;

    private final YopCircuitBreaker circuitBreaker;

    private final String sdkSource;

    private final String clientId;

    private final RouterPolicy routerPolicy;


    public ClientHandlerImpl(ClientHandlerParams handlerParams) {
        this.provider = handlerParams.getClientParams().getProvider();
        this.env = handlerParams.getClientParams().getEnv();
        this.yopCredentialsProvider = handlerParams.getClientParams().getCredentialsProvider();
        this.yopSdkConfigProvider = handlerParams.getClientParams().getYopSdkConfigProvider();
        this.platformCredentialsProvider = handlerParams.getClientParams().getPlatformCredentialsProvider();
        this.routeConfigProvider = handlerParams.getClientParams().getRouteConfigProvider();
        this.routerPolicy = handlerParams.getClientParams().getRouterPolicy();
        this.authorizationReqRegistry = handlerParams.getClientParams().getAuthorizationReqRegistry();
        this.clientId = handlerParams.getClientParams().getClientId();
        if (isBasicClient(clientId)) {
            sdkSource = YopConstants.YOP_SDK_SOURCE_BASIC;
        } else {
            sdkSource = YopConstants.YOP_SDK_SOURCE_BIZ;
        }
        this.serverRootSpace = new ServerRootSpace(provider, env, this.clientId, handlerParams.getClientParams().getEndPoint(),
                handlerParams.getClientParams().getYosEndPoint(), handlerParams.getClientParams().getPreferredEndPoint(),
                handlerParams.getClientParams().getPreferredYosEndPoint(), handlerParams.getClientParams().getSandboxEndPoint());

        this.client = buildHttpClient(handlerParams);
        this.circuitBreaker = new YopSentinelCircuitBreaker(this.serverRootSpace);
    }

    private YopHttpClient buildHttpClient(ClientHandlerParams handlerParams) {
        YopHttpClient yopHttpClient;
        if (null == handlerParams) {
            yopHttpClient = YopHttpClientFactory.getDefaultClient(provider, env, this.yopSdkConfigProvider);
        } else {
            yopHttpClient = YopHttpClientFactory.getClient(handlerParams.getClientParams().getClientConfiguration());
        }
        return yopHttpClient;
    }

    @Override
    public <Input extends BaseRequest, Output extends BaseResponse> Output execute(
            ClientExecutionParams<Input, Output> executionParams) {
        ClientUtils.setCurrentClientId(clientId);
        try {
            ExecutionContext executionContext = getExecutionContext(executionParams);
            return new UriResourceRouteInvokerWrapper<>(
                    new YopInvoker<>(executionParams, executionContext, true),
                    new SimpleUriRetryPolicy(executionParams.getInput().getRequestConfig().getMaxRetryCount()),
                    new YopRouter<>(serverRootSpace, routerPolicy)).invoke();
        } finally {
            ClientUtils.removeCurrentClientId();
        }
    }

    private interface YopCircuitBreaker {

        <Input extends BaseRequest, Output extends BaseResponse> Output execute(Request<Input> request,
                                                                                UriResourceRouteInvoker<ClientExecutionParams<Input, Output>, Output,
                                                                                        ExecutionContext, AnalyzedException> invoker);

    }

    private YopRouteConfig findRouteConfig(URI uri) {
        String configKey = StringUtils.strip(uri.getHost().replaceAll("[^a-zA-Z0-9]", "_")
                + (uri.getPort() > 0 ? uri.getPort() : ""), "_");
        // 指定配置
        YopRouteConfig routeConfig = routeConfigProvider.getRouteConfig(configKey);
        // 默认配置
        if (null == routeConfig) {
            routeConfig = routeConfigProvider.getRouteConfig();
        }
        // 兜底配置
        return null == routeConfig ? YopRouteConfig.DEFAULT_CONFIG : routeConfig;
    }

    private class YopSentinelCircuitBreaker implements YopCircuitBreaker {

        public YopSentinelCircuitBreaker(ServerRootSpace serverRootSpace) {
            Map<String, YopCircuitBreakerConfig> circuitBreakerConfigMap = Maps.newHashMap();
            serverRootSpace.getMainServers().forEach((serverRootType, uri) -> {
                YopRouteConfig routeConfig = findRouteConfig(uri);
                circuitBreakerConfigMap.put(new UriResource(
                        UriResource.computeResourceGroup(serverRootSpace.getProvider(),
                                serverRootSpace.getEnv(), serverRootSpace.getServerGroup(), serverRootType), uri)
                        .computeResourceKey(), routeConfig.getCircuitBreakerConfig());
            });

            serverRootSpace.getBackupServers().forEach((serverRootType, uris) -> {
                for (URI uri : uris) {
                    YopRouteConfig routeConfig = findRouteConfig(uri);
                    circuitBreakerConfigMap.put(new UriResource(
                            UriResource.computeResourceGroup(serverRootSpace.getProvider(),
                                    serverRootSpace.getEnv(), serverRootSpace.getServerGroup(), serverRootType), uri)
                            .computeResourceKey(), routeConfig.getCircuitBreakerConfig());
                }
            });
            YopDegradeRuleHelper.initDegradeRule(circuitBreakerConfigMap);
        }

        @Override
        public <Input extends BaseRequest, Output extends BaseResponse> Output execute(Request<Input> request,
                                                                                       UriResourceRouteInvoker<ClientExecutionParams<Input, Output>, Output,
                                                                                               ExecutionContext, AnalyzedException> invoker)
                throws YopClientException, YopHttpException, YopUnknownException, YopHostException {

            Entry entry = null;
            boolean successInvoked = false;
            final UriResource uriResource = invoker.getUriResource();
            try {
                // 请求保留资源时，不再熔断
                if (!uriResource.isRetained()) {
                    YopRouteConfig routeConfig = findRouteConfig(uriResource.getResource());
                    final String resourceKey = uriResource.computeResourceKey();
                    YopDegradeRuleHelper.addDegradeRule(resourceKey, routeConfig.getCircuitBreakerConfig());
                    entry = YopSph.getInstance().entry(resourceKey);
                }
                final Output output = doExecute(request, invoker);
                successInvoked = true;
                return output;
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
                if (null != uriResource && null != uriResource.getCallback()) {
                    uriResource.getCallback().notify(successInvoked);
                }
                if (null != entry) {
                    final AnalyzedException lastException = invoker.getLastException();
                    if (!successInvoked && null != lastException && lastException.isNeedDegrade()) {
                        Tracer.trace(lastException.getException());
                    }
                    entry.exit();
                }
            }
        }
    }

    private <Input extends BaseRequest, Output extends BaseResponse> Output doExecute(Request<Input> request,
                                                                                      UriResourceRouteInvoker<ClientExecutionParams<Input, Output>, Output,
                                                                                              ExecutionContext, AnalyzedException> invoker)
            throws YopClientException, YopHttpException, YopUnknownException {

        final long start = System.currentTimeMillis();
        URI serverRoot = request.getEndpoint();
        Throwable throwable = null;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Begin HttpInvoke, server:{}, resource:{}", serverRoot, request.getResourcePath());
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
                final AnalyzedException analyzeResult = invoker.getExceptionAnalyzer().analyze(throwable);
                invoker.addException(analyzeResult);
                result = "fail caused by " + analyzeResult.getExDetail();
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Finish HttpInvoke, server:{}, resource:{}, result:{}, elapsed:{}.",
                        serverRoot, request.getResourcePath(), result, System.currentTimeMillis() - start);
            }
        }
    }

    private class YopInvoker<Input extends BaseRequest, Output extends BaseResponse>
            extends AbstractUriResourceRouteInvoker<ClientExecutionParams<Input, Output>, Output,
            ExecutionContext, AnalyzedException> {

        public YopInvoker(ClientExecutionParams<Input, Output> executionParams,
                          ExecutionContext executionContext,
                          boolean circuitBreaker) {
            setInput(executionParams);
            setContext(executionContext);
            if (circuitBreaker) {
                enableCircuitBreaker();
            } else {
                disableCircuitBreaker();
            }
        }

        @Override
        public ExceptionAnalyzer<AnalyzedException> getExceptionAnalyzer() {
            Set<String> excludeExceptions = Collections.emptySet();
            Set<String> retryExceptions = Collections.emptySet();
            final UriResource uriResource = getUriResource();
            final YopRouteConfig routeConfig = findRouteConfig(uriResource.getResource());
            if (null != routeConfig) {
                if (null != routeConfig.getCircuitBreakerConfig()
                        && null != routeConfig.getCircuitBreakerConfig().getExcludeExceptions()) {
                    excludeExceptions = routeConfig.getCircuitBreakerConfig().getExcludeExceptions();
                }
                if (null != routeConfig.getRetryExceptions()) {
                    retryExceptions = routeConfig.getRetryExceptions();
                }
            }
            return SimpleExceptionAnalyzer.from(excludeExceptions, retryExceptions);
        }

        @Override
        public Output invoke() {
            // 准备http参数
            Request<Input> request = getInput().getRequestMarshaller().marshall(getInput().getInput());
            request.setEndpoint(getUriResource().getResource());
            request.addHeader(Headers.YOP_SDK_SOURCE, sdkSource);

            // 发起http调用
            if (isCircuitBreakerEnable() && null != circuitBreaker && !BooleanUtils.isFalse(request.getOriginalRequestObject()
                    .getRequestConfig().getEnableCircuitBreaker())) {
                return circuitBreaker.execute(request, this);
            } else {
                return doExecute(request, this);
            }
        }
    }

    private <Output extends BaseResponse, Input extends BaseRequest> ExecutionContext getExecutionContext(
            ClientExecutionParams<Input, Output> executionParams) {
        AuthorizationReq authorizationReq = getAuthorizationReq(executionParams.getInput());
        if (authorizationReq == null) {
            throw new YopClientException("no authenticate req defined");
        } else {
            YopRequestConfig requestConfig = executionParams.getInput().getRequestConfig();
            YopCredentials<?> credential = getCredentials(provider, env, requestConfig, authorizationReq);
            YopEncryptor encryptor = null;
            Future<EncryptOptions> encryptOptions = null;
            if (isEncryptSupported(credential, requestConfig)) {
                encryptor = getEncryptor(requestConfig);
                encryptOptions = EncryptOptionsCache.loadEncryptOptions(provider, env, credential.getAppKey(),
                        requestConfig.getEncryptAlg(), requestConfig.getServerRoot());
            }
            ExecutionContext.Builder builder = ExecutionContext.Builder.anExecutionContext()
                    .withProvider(provider)
                    .withEnv(env)
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
        List<CertTypeEnum> availableCerts = checkAvailableCerts(customAppKey, provider, env);
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

    private List<CertTypeEnum> checkAvailableCerts(String appKey, String provider, String env) {
        List<CertTypeEnum> configPrivateCerts = yopCredentialsProvider.getSupportCertTypes(provider, env, appKey);
        if (CollectionUtils.isEmpty(configPrivateCerts)) {
            throw new YopClientException("can not find private key for provider:"
                    + provider + ",env:" + env + ",appKey:" + appKey);
        }
        return configPrivateCerts;
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }
}
