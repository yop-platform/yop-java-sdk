package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.CertTypeEnum;

import java.util.Collections;
import java.util.List;

/**
 * title: Interface for providing YOP credentials.<br>
 * description:
 * Implementations are free to use any
 * strategy for providing YOP credentials, such as simply providing static
 * credentials that don't change, or more complicated implementations, such as
 * integrating with existing key management systems.<br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/11/14 17:27
 */
public interface YopCredentialsProvider {

    /**
     * Returns Credentials which the caller can use to authorize an YOP request.
     * Each implementation of YOPCredentialsProvider can chose its own strategy for
     * loading credentials.  For example, an implementation might load credentials
     * from an existing key managemenYopSignProcessort system, or load new credentials when
     * credentials are rotated.
     * <p>
     * 根据appKey与凭证类型(CertTypeEnum)，加载可用凭证，用于Yop请求的认证
     * 商户可根据自身情况将凭证信息存储在安全的地方，可通过缓存提升性能
     *
     * @param appKey         appKey:应用标识
     * @param credentialType credentialType:凭证类型(CertTypeEnum)
     * @return YOPCredentials which the caller can use to authorize an YOP request.
     */
    YopCredentials<?> getCredentials(String appKey, String credentialType);

    /**
     * 根据appKey获取应用下可用密钥类型
     *
     * @param appKey 应用标识
     * @return 密钥类型列表
     */
    List<CertTypeEnum> getSupportCertTypes(String appKey);

    /**
     * 单应用时，用于加载默认配置
     * 多应用时，用于指定默认应用(自定义provider时，须覆盖实现)
     *
     * @return appKey 默认应用
     */
    default String getDefaultAppKey() {
        return "default";
    }

    /**
     * Returns symmetrical keys for using when decrypt yop certs from remote
     * may be different by appKey
     * may be not only one for retry
     *
     * @param appKey 应用标识
     * @return 加密密钥
     */
    @Deprecated
    default List<YopCertConfig> getIsvEncryptKey(String appKey) {
        return Collections.emptyList();
    }

}
