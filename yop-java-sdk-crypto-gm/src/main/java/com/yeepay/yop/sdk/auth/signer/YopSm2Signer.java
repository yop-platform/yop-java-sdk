/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.security.SignerTypeEnum;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.Security;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * title: YopSm2Signer<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:25 下午
 */
public class YopSm2Signer extends AbstractYopPKISigner {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSm2Signer.class);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    protected Map<DigestAlgEnum, MessageDigest> initMdInstance() {
        try {
            return Collections.singletonMap(DigestAlgEnum.SM3, MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME));
        } catch (Exception e) {
            LOGGER.warn("Unable to get Digest Function, will fail when use YopRSASigner for sign, ex", e);
        }
        return Collections.emptyMap();
    }

    @Override
    public List<String> supportSignerAlg() {
        return Lists.newArrayList(SignerTypeEnum.SM2.name());
    }
}
