/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopCredentialsProviderRegistry;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProvider;
import com.yeepay.yop.sdk.auth.credentials.provider.YopPlatformCredentialsProviderRegistry;
import com.yeepay.yop.sdk.base.config.provider.YopSdkConfigProviderRegistry;
import com.yeepay.yop.sdk.client.ClientParams;
import com.yeepay.yop.sdk.config.provider.YopSdkConfigProvider;
import com.yeepay.yop.sdk.service.common.YopClient;
import com.yeepay.yop.sdk.service.common.YopClientBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.COLON;

/**
 * title: 客户端缓存<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/1/9
 */
public class ClientUtils {

    private static final String BASIC_CLIENT_PREFIX = YopClientBuilder.class.getCanonicalName();

    private static final String INNER_CLIENT_TYPE = "inner";

    // 当前线程上下文中的clientId
    private static ThreadLocal<String> CURRENT_CLIENT_ID = new ThreadLocal<>();

    // 各个环境的不同配置的基础Client列表<{provider}####{env}, List<{clientId}>>
    private static final Map<String, List<String>> INNER_BASIC_CLIENT_MAP = Maps.newConcurrentMap();

    // 各个环境的不同配置的Client实例<{clientId}, {ClientInst}>
    private static final Map<String, Object> CLIENT_INST_MAP = Maps.newConcurrentMap();

    // 各个环境的不同Client的配置<{clientId}, {ClientParams}>
    private static final Map<String, ClientParams> CLIENT_CONFIG_MAP = Maps.newConcurrentMap();

    /**
     * 根据client配置获取client实例
     *
     * @param clientParams ClientParams
     * @param <ClientInst> client实例类型
     * @return client实例
     */
    public static <ClientInst> ClientInst getOrBuildClientInst(ClientParams clientParams, ClientInstBuilder<ClientInst> instBuilder) {
        final ClientInst clientInst = instBuilder.build(clientParams);
        CLIENT_INST_MAP.put(clientParams.getClientId(), clientInst);

        if (!isInnerBasicClient(clientParams.getClientId())) {
            final String innerBasicClientId = toInnerBasicClientId(clientParams.getClientId());
            CLIENT_INST_MAP.put(innerBasicClientId, YopClientBuilder.builder()
                    .withInner(true)
                    .withClientId(innerBasicClientId)
                    .withProvider(clientParams.getProvider())
                    .withEnv(clientParams.getEnv())
                    .withCredentialsProvider(clientParams.getCredentialsProvider())
                    .withYopSdkConfigProvider(clientParams.getYopSdkConfigProvider())
                    .withPlatformCredentialsProvider(clientParams.getPlatformCredentialsProvider())
                    .withRouteConfigProvider(clientParams.getRouteConfigProvider())
                    .withRouterPolicy(clientParams.getRouterPolicy())
                    .withClientConfiguration(clientParams.getClientConfiguration())
                    .withEndpoint(null != clientParams.getEndPoint() ? clientParams.getEndPoint().toString() : null)
                    .withYosEndpoint(null != clientParams.getYosEndPoint() ? clientParams.getYosEndPoint().toString() : null)
                    .withPreferredEndPoint(clientParams.getPreferredEndPoint())
                    .withPreferredYosEndPoint(clientParams.getPreferredYosEndPoint())
                    .withSandboxEndPoint(null != clientParams.getSandboxEndPoint() ? clientParams.getSandboxEndPoint().toString() : null)
                    .build());
        }
        return clientInst;
    }

    public static <ClientInst> ClientInst getClientInst(String clientId) {
        return (ClientInst) CLIENT_INST_MAP.get(clientId);
    }

    public static boolean isBasicClient(String clientId) {
        return StringUtils.startsWith(clientId, BASIC_CLIENT_PREFIX + COLON);
    }

    public static boolean isInnerBasicClient(String clientId) {
        return StringUtils.startsWith(clientId, BASIC_CLIENT_PREFIX + COLON + INNER_CLIENT_TYPE + COLON);
    }

    public static String toInnerBasicClientId(String clientId) {
        if (isInnerBasicClient(clientId)) {
            return clientId;
        }
        return BASIC_CLIENT_PREFIX + COLON + INNER_CLIENT_TYPE + COLON + clientId.substring(clientId.indexOf(COLON) + 1);
    }

    public static String computeClientIdSuffix(ClientParams clientParams) {
        // TODO 优化，可以根据配置参数来避免用户重复构造client（但要考虑到用户根据业务拆分client的需要）
        return UUID.randomUUID().toString();
    }

    public static void cacheClientConfig(String clientId, ClientParams clientParams) {
        CLIENT_CONFIG_MAP.put(clientId, clientParams);
        if (isInnerBasicClient(clientId)) {
            INNER_BASIC_CLIENT_MAP.computeIfAbsent(getClientEnvCacheKey(clientParams.getProvider(), clientParams.getEnv()),
                    p -> new LinkedList<>()).add(clientId);
        }
    }

    private static String getClientEnvCacheKey(String provider, String env) {
        return StringUtils.defaultString(provider, YOP_DEFAULT_PROVIDER)
                + COLON + StringUtils.defaultString(env, YOP_DEFAULT_ENV);
    }

    public static String getCurrentClientId() {
        return CURRENT_CLIENT_ID.get();
    }

    public static YopClient getDefaultYopClient() {
        return getAvailableYopClient(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV);
    }

    /**
     * 获取可用的基础SDK调用client
     *
     * @param provider 服务方
     * @param env 环境
     * @return YopClient
     */
    public static YopClient getAvailableYopClient(String provider, String env) {
        YopClient clientInst = null;
        String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            clientInst = getClientInst(toInnerBasicClientId(currentClientId));
        }
        if (null == clientInst) {
            final String clientEnvCacheKey = getClientEnvCacheKey(provider, env);
            final List<String> clientIds = INNER_BASIC_CLIENT_MAP.get(clientEnvCacheKey);
            if (CollectionUtils.isNotEmpty(clientIds)) {
                clientInst = getClientInst(clientIds.get(0));
            }
        }
        if (null == clientInst) {
            clientInst = YopClientBuilder.builder().withProvider(provider).withEnv(env).build();
        }
        return clientInst;
    }

    public static void setCurrentClientId(String clientId) {
        CURRENT_CLIENT_ID.set(clientId);
    }

    public static void removeCurrentClientId() {
        CURRENT_CLIENT_ID.remove();
    }

    public static YopSdkConfigProvider getCurrentSdkConfigProvider() {
        final String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            final ClientParams clientParams = CLIENT_CONFIG_MAP.get(currentClientId);
            if (null != clientParams || null != clientParams.getYopSdkConfigProvider()) {
                return clientParams.getYopSdkConfigProvider();
            }
        }
        return YopSdkConfigProviderRegistry.getProvider();
    }

    public static YopCredentialsProvider getCurrentCredentialsProvider() {
        final String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            final ClientParams clientParams = CLIENT_CONFIG_MAP.get(currentClientId);
            if (null != clientParams || null != clientParams.getCredentialsProvider()) {
                return clientParams.getCredentialsProvider();
            }
        }
        return YopCredentialsProviderRegistry.getProvider();
    }

    public static YopPlatformCredentialsProvider getCurrentPlatformCredentialsProvider() {
        final String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            final ClientParams clientParams = CLIENT_CONFIG_MAP.get(currentClientId);
            if (null != clientParams || null != clientParams.getPlatformCredentialsProvider()) {
                return clientParams.getPlatformCredentialsProvider();
            }
        }
        return YopPlatformCredentialsProviderRegistry.getProvider();
    }

    public interface ClientInstBuilder<T> {
        T build(ClientParams clientParams);
    }

}
