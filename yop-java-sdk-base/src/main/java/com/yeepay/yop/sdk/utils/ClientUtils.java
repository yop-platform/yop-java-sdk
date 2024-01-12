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
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_ENV;
import static com.yeepay.yop.sdk.YopConstants.YOP_DEFAULT_PROVIDER;
import static com.yeepay.yop.sdk.constants.CharacterConstants.EMPTY;
import static com.yeepay.yop.sdk.constants.CharacterConstants.HASH;

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

    // 当前线程上下文中的clientId
    private static InheritableThreadLocal<String> CURRENT_CLIENT_ID = new InheritableThreadLocal<>();

    // 各个环境的不同配置的基础Client列表<{provider}####{env}, List<{clientId}>>
    private static final Map<String, List<String>> BASIC_CLIENT_MAP = Maps.newConcurrentMap();

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
        return (ClientInst) CLIENT_INST_MAP.computeIfAbsent(clientParams.getClientId(), p -> {
            if (!isBasicClient(p)) {
                final String basicClientId = toBasicClientId(p);
                CLIENT_INST_MAP.computeIfAbsent(basicClientId, h -> YopClientBuilder.builder()
                        .withProvider(clientParams.getProvider())
                        .withEnv(clientParams.getEnv())
                        .withCredentialsProvider(clientParams.getCredentialsProvider())
                        .withYopSdkConfigProvider(clientParams.getYopSdkConfigProvider())
                        .withPlatformCredentialsProvider(clientParams.getPlatformCredentialsProvider())
                        .withClientConfiguration(clientParams.getClientConfiguration())
                        .withEndpoint(null != clientParams.getEndPoint() ? clientParams.getEndPoint().toString() : null)
                        .withYosEndpoint(null != clientParams.getYosEndPoint() ? clientParams.getYosEndPoint().toString() : null)
                        .withPreferredEndPoint(clientParams.getPreferredEndPoint())
                        .withPreferredYosEndPoint(clientParams.getPreferredYosEndPoint())
                        .withSandboxEndPoint(null != clientParams.getSandboxEndPoint() ? clientParams.getSandboxEndPoint().toString() : null)
                        .build());

            }
            return instBuilder.build(clientParams);
        });
    }

    public static <ClientInst> ClientInst getClientInst(String clientId) {
        return (ClientInst) CLIENT_INST_MAP.get(clientId);
    }

    public static boolean isBasicClient(String clientId) {
        return StringUtils.startsWith(clientId, BASIC_CLIENT_PREFIX);
    }

    public static String toBasicClientId(String clientId) {
        return BASIC_CLIENT_PREFIX + HASH + clientId.substring(0, clientId.indexOf(HASH));
    }

    public static String computeClientIdSuffix(ClientParams clientParams) {
        // TODO 确认后缀参数
        return StringUtils.defaultString(clientParams.getProvider(), EMPTY) +
                HASH + StringUtils.defaultString(clientParams.getEnv(), EMPTY) +
                HASH + clientParams.getEndPoint() +
                HASH + clientParams.getYosEndPoint() +
                HASH + clientParams.getPreferredEndPoint() +
                HASH + clientParams.getPreferredYosEndPoint() +
                HASH + clientParams.getSandboxEndPoint() +
                HASH + clientParams.getClientId() +
                HASH + (null == clientParams.getCredentialsProvider() ? EMPTY : clientParams.getCredentialsProvider().hashCode()) +
                HASH + (null == clientParams.getYopSdkConfigProvider() ? EMPTY : clientParams.getYopSdkConfigProvider().hashCode()) +
                HASH + (null == clientParams.getPlatformCredentialsProvider() ? EMPTY : clientParams.getPlatformCredentialsProvider().hashCode());
    }

    public static void cacheClientConfig(String clientId, ClientParams clientParams) {
        CLIENT_CONFIG_MAP.put(clientId, clientParams);
    }

    public static String getCurrentClientId() {
        return CURRENT_CLIENT_ID.get();
    }

    public static YopClient getCurrentBasicClient() {
        return getCurrentBasicClient(YOP_DEFAULT_PROVIDER, YOP_DEFAULT_ENV);
    }

    public static YopClient getCurrentBasicClient(String provider, String env) {
        final String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            return getClientInst(toBasicClientId(currentClientId));
        }
        return YopClientBuilder.builder().withProvider(provider).withEnv(env).build();
    }

    public static void setCurrentClientId(String clientId) {
        CURRENT_CLIENT_ID.set(clientId);
    }

    public static void removeCurrentClientId() {
        CURRENT_CLIENT_ID.remove();
    }

    public static YopSdkConfigProvider getCurrentYopSdkConfigProvider() {
        final String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            final ClientParams clientParams = CLIENT_CONFIG_MAP.get(currentClientId);
            if (null != clientParams || null != clientParams.getYopSdkConfigProvider()) {
                return clientParams.getYopSdkConfigProvider();
            }
        }
        return YopSdkConfigProviderRegistry.getProvider();
    }

    public static YopCredentialsProvider getCurrentYopCredentialsProvider() {
        final String currentClientId = getCurrentClientId();
        if (StringUtils.isNotBlank(currentClientId)) {
            final ClientParams clientParams = CLIENT_CONFIG_MAP.get(currentClientId);
            if (null != clientParams || null != clientParams.getCredentialsProvider()) {
                return clientParams.getCredentialsProvider();
            }
        }
        return YopCredentialsProviderRegistry.getProvider();
    }

    public static YopPlatformCredentialsProvider getCurrentYopPlatformCredentialsProvider() {
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
