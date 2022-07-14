/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.auth.signer;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.SignerTypeEnum;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: YopSignerFactory<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/21 2:26 上午
 */
public class YopSignerFactory {

    private static final Map<String, YopSigner> YOP_SIGNER_MAP = Maps.newHashMapWithExpectedSize(3);

    static {
        ServiceLoader<YopSigner> yopSignerLoader = ServiceLoader.load(YopSigner.class);
        for (YopSigner yopSigner : yopSignerLoader) {
            for (String signerType : yopSigner.supportSignerAlg()) {
                YOP_SIGNER_MAP.put(signerType, yopSigner);
            }
        }
    }

    public static void registerSigner(String signerType, YopSigner signer) {
        YOP_SIGNER_MAP.put(signerType, signer);
    }

    @Deprecated
    public static void registerSigner(SignerTypeEnum signerType, YopSigner signer) {
        registerSigner(signerType.name(), signer);
    }

    public static YopSigner getSigner(String signerType) {
        final YopSigner yopSigner = YOP_SIGNER_MAP.get(signerType);
        if (null == yopSigner) {
            throw new YopClientException("YopSigner not found, signerType:" + signerType);
        }
        return yopSigner;
    }

    @Deprecated
    public static YopSigner getSigner(SignerTypeEnum signerType) {
        return getSigner(signerType.name());
    }

}
