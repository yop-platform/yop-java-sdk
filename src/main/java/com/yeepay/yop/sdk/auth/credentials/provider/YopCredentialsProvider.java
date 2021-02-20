package com.yeepay.yop.sdk.auth.credentials.provider;

import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;

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
     * from an existing key management system, or load new credentials when
     * credentials are rotated.
     *
     * @param appKey         appKey
     * @param credentialType credentialType
     * @return YOPCredentials which the caller can use to authorize an YOP request.
     */
    YopCredentials getCredentials(String appKey, String credentialType);

    /**
     * Returns symmetrical keys for using when decrypt yop certs from remote
     * may be different by appKey
     * may be not only one for retry
     * @param appKey
     * @return
     */
    YopCertConfig[] getYopEncryptKey(String appKey);

}
