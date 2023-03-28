package com.yeepay.yop.sdk.client;

import com.google.common.collect.Sets;
import com.netflix.hystrix.*;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.req.AuthorizationReq;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqRegistry;
import com.yeepay.yop.sdk.auth.req.AuthorizationReqSupport;
import com.yeepay.yop.sdk.base.auth.signer.YopSignerFactory;
import com.yeepay.yop.sdk.base.cache.EncryptOptionsCache;
import com.yeepay.yop.sdk.client.router.GateWayRouter;
import com.yeepay.yop.sdk.client.router.ServerRootSpace;
import com.yeepay.yop.sdk.client.router.SimpleGateWayRouter;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.exception.YopHostException;
import com.yeepay.yop.sdk.exception.YopHttpException;
import com.yeepay.yop.sdk.exception.YopServerException;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Set;
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

    private final YopHttpClient client;

    private final GateWayRouter gateWayRouter;

    public ClientHandlerImpl(ClientHandlerParams handlerParams) {
        this.yopCredentialsProvider = handlerParams.getClientParams().getCredentialsProvider();
        this.authorizationReqRegistry = handlerParams.getClientParams().getAuthorizationReqRegistry();
        ServerRootSpace serverRootSpace = new ServerRootSpace(handlerParams.getClientParams().getEndPoint(),
                handlerParams.getClientParams().getYosEndPoint(), handlerParams.getClientParams().getPreferredEndPoint(),
                handlerParams.getClientParams().getPreferredYosEndPoint(), handlerParams.getClientParams().getSandboxEndPoint());
        this.gateWayRouter = new SimpleGateWayRouter(serverRootSpace);
        this.client = buildHttpClient(handlerParams);
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
        for (URI endPoint : endPoints) {
            // TODO 重复marshall，multipart 是否可读
            Request<Input> request = executionParams.getRequestMarshaller().marshall(executionParams.getInput());
            request.setEndpoint(endPoint);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Trying Host, value:{}", endPoint);
            }
            try {
                return new ClientExecuteCommand<Input, Output>(
                        configToSetter(new HystrixConfig(StringUtils.substringBefore(endPoint.toString(), "?"))),
                        executionContext, request, executionParams.getResponseHandler()).execute();
            } catch (HystrixBadRequestException e) {// 客户端异常
                LOGGER.error("Client Error, ex:", e);
                throw (YopClientException) e.getCause();
            } catch (HystrixRuntimeException e) {// Hystrix异常
                switch (e.getFailureType()) {
                    // 当笔切换，重试
                    case SHORTCIRCUIT:
                    case REJECTED_THREAD_EXECUTION:
                    case REJECTED_SEMAPHORE_FALLBACK:
                    case REJECTED_SEMAPHORE_EXECUTION:
                        continue;
                    case BAD_REQUEST_EXCEPTION:
                        throw new YopClientException(ExceptionUtils.getMessage(e), ExceptionUtils.getRootCause(e));
                    case COMMAND_EXCEPTION:
                        if (e.getCause() instanceof YopHostException) {
                            continue;
                        }
                        if (e.getCause() instanceof YopHttpException) {
                            throw (YopHttpException) e.getCause();
                        }
                    case TIMEOUT:// 超时、或其他未知异常不再重试
                    default:
                        handleUnExpectedError(e);
                }
            } catch (Exception e) {// 其他异常
                handleUnExpectedError(e);
            } finally {
                // report
            }
        }
        // 理论上不会到这里
        throw new YopClientException("All Hosts Not Available, value:" + endPoints);
    }

    private void handleUnExpectedError(Exception ex) {
        LOGGER.error("UnExpected Error, ex:", ex);
        throw new YopServerException("UnExpected Error, " + ExceptionUtils.getMessage(ex), ExceptionUtils.getRootCause(ex));
    }

    private static Set<String> RETRY_EXCEPTIONS = Sets.newHashSet("java.net.UnknownHostException");

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
                return client.execute(request, request.getOriginalRequestObject().getRequestConfig(),
                        executionContext, responseHandler);
            } catch (YopHttpException e) {
                final Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (null == rootCause) {
                    throw e;
                }
                // 当笔重试 (连接异常)
                if (RETRY_EXCEPTIONS.contains(rootCause.getClass().getCanonicalName())) {
                    throw new YopHostException("Need Change Host, ", e);
                }

                // 其他异常，计入短路
                throw e;
            } catch (YopClientException e) {
                // 不计入短路
                throw new HystrixBadRequestException(getCommandKey() + " Fail, ", e);
            }
        }

    }

    private HystrixCommand.Setter configToSetter(HystrixConfig config) {
        return HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(config.getGroupKey()))
                .andCommandKey(HystrixCommandKey.Factory.asKey(config.getCommandKey()))
                .andCommandPropertiesDefaults(HystrixCommandProperties.defaultSetter()
                        .withCircuitBreakerEnabled(config.isCircuitBreakerEnabled())
                        .withCircuitBreakerRequestVolumeThreshold(config.getCircuitBreakerRequestVolumeThreshold())
                        .withCircuitBreakerErrorThresholdPercentage(config.getCircuitBreakerErrorThresholdPercentage())
                        .withCircuitBreakerSleepWindowInMilliseconds(config.getCircuitBreakerSleepWindowInMilliseconds())
                        .withCircuitBreakerForceClosed(config.isCircuitBreakerForceClosed())
                        .withCircuitBreakerForceOpen(config.isCircuitBreakerForceOpen())
                        .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.valueOf(config.getExecutionIsolationStrategy()))
                        .withExecutionTimeoutEnabled(false)// hystrix 超时不做控制，留给http协议处理
                        .withExecutionTimeoutInMilliseconds(config.getExecutionIsolationThreadTimeoutInMilliseconds())// 基于上一个配置，超时时间是无效的
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

    public static class HystrixConfig implements Serializable {

        private static final long serialVersionUID = -1L;

        static final HystrixConfig DEFAULT_CONFIG = new HystrixConfig();

        public HystrixConfig() {
        }

        public HystrixConfig(String commandKey) {
            this.commandKey = commandKey;
        }

        // 命令、分组、线程池
        /**
         * 命令键：该值会关联到断路器
         */
        private String commandKey = YopConstants.DEFAULT_SERVER_ROOT;

        /**
         * 命令分组
         */
        private String groupKey = "YOP_SERVER_ROOT";

        /**
         * 线程池
         */
        private String threadPoolKey = "DefaultClientExecutePool";

        // region 执行控制
        /**
         * 隔离策略：线程(与调用线程隔离)
         */
        private String executionIsolationStrategy = "THREAD";

        /**
         * 启用超时熔断
         */
        private boolean executionTimeoutEnabled = true;

        /**
         * 线程超时时长(毫秒)
         * 默认1000
         */
        private int executionIsolationThreadTimeoutInMilliseconds = 60000;// 总开关关闭后，该选项是否还有效

        /**
         * 是否允许超时中断
         */
        private boolean executionIsolationThreadInterruptOnTimeout = false;// hystrix不做超时控制，交给httpClient

        /**
         * 是否允许取消中断
         */
        private boolean executionIsolationThreadInterruptOnCancel = false;

        /**
         * 最大并发（针对隔离策略：SEMAPHORE）
         * 默认10
         */
        private int executionIsolationSemaphoreMaxConcurrentRequests = 100;// TODO确认该值影响
        // endregion

        // region 断路器

        /**
         * 启用断路器
         */
        private boolean circuitBreakerEnabled = true;

        /**
         * 请求量阈值
         * 默认20
         */
        private int circuitBreakerRequestVolumeThreshold = 10;//TODO 达到该数量，才会检查健康数据，进而熔断

        /**
         * 错误率阈值
         */
        private int circuitBreakerErrorThresholdPercentage = 50;// TODO 确认商户容忍程度

        /**
         * 熔断时长(毫秒)
         */
        private int circuitBreakerSleepWindowInMilliseconds = 5000;// 该窗口期后，会进入半开

        /**
         * 强制打开断路器
         */
        private boolean circuitBreakerForceOpen = false;

        /**
         * 强制关闭断路器
         */
        private boolean circuitBreakerForceClosed = false;
        // endregion

        // region 断路器监控

        /**
         * 滑动窗口时长(毫秒)
         */
        private int cbMetricsRollingStatsTimeInMilliseconds = 10000;

        /**
         * 滑动窗口分桶
         */
        private int cbMetricsRollingStatsNumBuckets = 10;

        /**
         * 启用percentile
         */
        private boolean cbMetricsRollingPercentileEnabled = true;

        /**
         * percentile窗口时长(毫秒)
         */
        private int cbMetricsRollingPercentileTimeInMilliseconds = 60000;

        /**
         * percentile窗口分桶
         */
        private int cbMetricsRollingPercentileNumBuckets = 6;

        /**
         * percentile桶容量最大值(注：超过将丢弃，值越大越耗费资源)
         */
        private int cbMetricsRollingPercentileBucketSize = 100;

        /**
         * 健康快照窗口时长(毫秒)
         */
        private int cbMetricsHealthSnapshotIntervalInMilliseconds = 500;
        // endregion

        // region 线程池参数
        private int coreSize = 10;// TODO 确认线程池
        // 默认 10
        private int maximumSize = 50;

        // 默认不排队-1
        private int maxQueueSize = 50;

        /**
         * maxQueueSize > 0 时生效
         * 场景：maxQueueSize设定后，可以通过该参数动态模拟拒绝门槛
         * 默认5
         */
        private int queueSizeRejectionThreshold = 50;

        /**
         * 线程存活时长(分钟)
         * 默认1分钟
         */
        private int keepAliveTimeMinutes = 1;

        /**
         * max线程数生效开关
         * 默认false
         */
        private boolean allowMaximumSizeToDivergeFromCoreSize = true;

        /**
         * 线程池监控窗口时长(毫秒)
         */
        private int tpMetricsRollingStatsTimeInMilliseconds = 10000;

        /**
         * 线程池窗口分桶数量
         */
        private int tpMetricsRollingStatsNumBuckets = 10;
        // endregion


        public String getCommandKey() {
            return commandKey;
        }

        public void setCommandKey(String commandKey) {
            this.commandKey = commandKey;
        }

        public String getGroupKey() {
            return groupKey;
        }

        public void setGroupKey(String groupKey) {
            this.groupKey = groupKey;
        }

        public String getThreadPoolKey() {
            return threadPoolKey;
        }

        public void setThreadPoolKey(String threadPoolKey) {
            this.threadPoolKey = threadPoolKey;
        }

        public String getExecutionIsolationStrategy() {
            return executionIsolationStrategy;
        }

        public void setExecutionIsolationStrategy(String executionIsolationStrategy) {
            this.executionIsolationStrategy = executionIsolationStrategy;
        }

        public boolean isExecutionTimeoutEnabled() {
            return executionTimeoutEnabled;
        }

        public void setExecutionTimeoutEnabled(boolean executionTimeoutEnabled) {
            this.executionTimeoutEnabled = executionTimeoutEnabled;
        }

        public int getExecutionIsolationThreadTimeoutInMilliseconds() {
            return executionIsolationThreadTimeoutInMilliseconds;
        }

        public void setExecutionIsolationThreadTimeoutInMilliseconds(int executionIsolationThreadTimeoutInMilliseconds) {
            this.executionIsolationThreadTimeoutInMilliseconds = executionIsolationThreadTimeoutInMilliseconds;
        }

        public boolean isExecutionIsolationThreadInterruptOnTimeout() {
            return executionIsolationThreadInterruptOnTimeout;
        }

        public void setExecutionIsolationThreadInterruptOnTimeout(boolean executionIsolationThreadInterruptOnTimeout) {
            this.executionIsolationThreadInterruptOnTimeout = executionIsolationThreadInterruptOnTimeout;
        }

        public boolean isExecutionIsolationThreadInterruptOnCancel() {
            return executionIsolationThreadInterruptOnCancel;
        }

        public void setExecutionIsolationThreadInterruptOnCancel(boolean executionIsolationThreadInterruptOnCancel) {
            this.executionIsolationThreadInterruptOnCancel = executionIsolationThreadInterruptOnCancel;
        }

        public int getExecutionIsolationSemaphoreMaxConcurrentRequests() {
            return executionIsolationSemaphoreMaxConcurrentRequests;
        }

        public void setExecutionIsolationSemaphoreMaxConcurrentRequests(int executionIsolationSemaphoreMaxConcurrentRequests) {
            this.executionIsolationSemaphoreMaxConcurrentRequests = executionIsolationSemaphoreMaxConcurrentRequests;
        }

        public boolean isCircuitBreakerEnabled() {
            return circuitBreakerEnabled;
        }

        public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
            this.circuitBreakerEnabled = circuitBreakerEnabled;
        }

        public int getCircuitBreakerRequestVolumeThreshold() {
            return circuitBreakerRequestVolumeThreshold;
        }

        public void setCircuitBreakerRequestVolumeThreshold(int circuitBreakerRequestVolumeThreshold) {
            this.circuitBreakerRequestVolumeThreshold = circuitBreakerRequestVolumeThreshold;
        }

        public int getCircuitBreakerErrorThresholdPercentage() {
            return circuitBreakerErrorThresholdPercentage;
        }

        public void setCircuitBreakerErrorThresholdPercentage(int circuitBreakerErrorThresholdPercentage) {
            this.circuitBreakerErrorThresholdPercentage = circuitBreakerErrorThresholdPercentage;
        }

        public int getCircuitBreakerSleepWindowInMilliseconds() {
            return circuitBreakerSleepWindowInMilliseconds;
        }

        public void setCircuitBreakerSleepWindowInMilliseconds(int circuitBreakerSleepWindowInMilliseconds) {
            this.circuitBreakerSleepWindowInMilliseconds = circuitBreakerSleepWindowInMilliseconds;
        }

        public boolean isCircuitBreakerForceOpen() {
            return circuitBreakerForceOpen;
        }

        public void setCircuitBreakerForceOpen(boolean circuitBreakerForceOpen) {
            this.circuitBreakerForceOpen = circuitBreakerForceOpen;
        }

        public boolean isCircuitBreakerForceClosed() {
            return circuitBreakerForceClosed;
        }

        public void setCircuitBreakerForceClosed(boolean circuitBreakerForceClosed) {
            this.circuitBreakerForceClosed = circuitBreakerForceClosed;
        }

        public int getCbMetricsRollingStatsTimeInMilliseconds() {
            return cbMetricsRollingStatsTimeInMilliseconds;
        }

        public void setCbMetricsRollingStatsTimeInMilliseconds(int cbMetricsRollingStatsTimeInMilliseconds) {
            this.cbMetricsRollingStatsTimeInMilliseconds = cbMetricsRollingStatsTimeInMilliseconds;
        }

        public int getCbMetricsRollingStatsNumBuckets() {
            return cbMetricsRollingStatsNumBuckets;
        }

        public void setCbMetricsRollingStatsNumBuckets(int cbMetricsRollingStatsNumBuckets) {
            this.cbMetricsRollingStatsNumBuckets = cbMetricsRollingStatsNumBuckets;
        }

        public boolean isCbMetricsRollingPercentileEnabled() {
            return cbMetricsRollingPercentileEnabled;
        }

        public void setCbMetricsRollingPercentileEnabled(boolean cbMetricsRollingPercentileEnabled) {
            this.cbMetricsRollingPercentileEnabled = cbMetricsRollingPercentileEnabled;
        }

        public int getCbMetricsRollingPercentileTimeInMilliseconds() {
            return cbMetricsRollingPercentileTimeInMilliseconds;
        }

        public void setCbMetricsRollingPercentileTimeInMilliseconds(int cbMetricsRollingPercentileTimeInMilliseconds) {
            this.cbMetricsRollingPercentileTimeInMilliseconds = cbMetricsRollingPercentileTimeInMilliseconds;
        }

        public int getCbMetricsRollingPercentileNumBuckets() {
            return cbMetricsRollingPercentileNumBuckets;
        }

        public void setCbMetricsRollingPercentileNumBuckets(int cbMetricsRollingPercentileNumBuckets) {
            this.cbMetricsRollingPercentileNumBuckets = cbMetricsRollingPercentileNumBuckets;
        }

        public int getCbMetricsRollingPercentileBucketSize() {
            return cbMetricsRollingPercentileBucketSize;
        }

        public void setCbMetricsRollingPercentileBucketSize(int cbMetricsRollingPercentileBucketSize) {
            this.cbMetricsRollingPercentileBucketSize = cbMetricsRollingPercentileBucketSize;
        }

        public int getCbMetricsHealthSnapshotIntervalInMilliseconds() {
            return cbMetricsHealthSnapshotIntervalInMilliseconds;
        }

        public void setCbMetricsHealthSnapshotIntervalInMilliseconds(int cbMetricsHealthSnapshotIntervalInMilliseconds) {
            this.cbMetricsHealthSnapshotIntervalInMilliseconds = cbMetricsHealthSnapshotIntervalInMilliseconds;
        }

        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(int maximumSize) {
            this.maximumSize = maximumSize;
        }

        public int getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(int maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }

        public int getQueueSizeRejectionThreshold() {
            return queueSizeRejectionThreshold;
        }

        public void setQueueSizeRejectionThreshold(int queueSizeRejectionThreshold) {
            this.queueSizeRejectionThreshold = queueSizeRejectionThreshold;
        }

        public int getKeepAliveTimeMinutes() {
            return keepAliveTimeMinutes;
        }

        public void setKeepAliveTimeMinutes(int keepAliveTimeMinutes) {
            this.keepAliveTimeMinutes = keepAliveTimeMinutes;
        }

        public boolean isAllowMaximumSizeToDivergeFromCoreSize() {
            return allowMaximumSizeToDivergeFromCoreSize;
        }

        public void setAllowMaximumSizeToDivergeFromCoreSize(boolean allowMaximumSizeToDivergeFromCoreSize) {
            this.allowMaximumSizeToDivergeFromCoreSize = allowMaximumSizeToDivergeFromCoreSize;
        }

        public int getTpMetricsRollingStatsTimeInMilliseconds() {
            return tpMetricsRollingStatsTimeInMilliseconds;
        }

        public void setTpMetricsRollingStatsTimeInMilliseconds(int tpMetricsRollingStatsTimeInMilliseconds) {
            this.tpMetricsRollingStatsTimeInMilliseconds = tpMetricsRollingStatsTimeInMilliseconds;
        }

        public int getTpMetricsRollingStatsNumBuckets() {
            return tpMetricsRollingStatsNumBuckets;
        }

        public void setTpMetricsRollingStatsNumBuckets(int tpMetricsRollingStatsNumBuckets) {
            this.tpMetricsRollingStatsNumBuckets = tpMetricsRollingStatsNumBuckets;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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
