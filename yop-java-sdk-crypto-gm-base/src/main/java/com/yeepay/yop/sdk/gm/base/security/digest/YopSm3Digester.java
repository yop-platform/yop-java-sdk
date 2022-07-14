/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.base.security.digest;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.security.digest.YopDigester;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.DigestAlgEnum;
import com.yeepay.yop.sdk.gm.base.utils.SmUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * title: SM3摘要<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/6/26
 */
public class YopSm3Digester implements YopDigester {

    private static final Logger LOGGER = LoggerFactory.getLogger(YopSm3Digester.class);

    private final ThreadLocal<Map<String, MessageDigest>> MESSAGE_DIGEST = ThreadLocal.withInitial(YopSm3Digester::initMdInstance);

    static {
        SmUtils.init();
    }

    @Override
    public List<String> supportedAlgs() {
        return Collections.singletonList(DigestAlgEnum.SM3.name());
    }

    @Override
    public byte[] digest(InputStream input, String alg) {
        try {
            MessageDigest md = getMessageDigestInstance(alg);
            DigestInputStream digestInputStream = new DigestInputStream(input, md);
            byte[] buffer = new byte[1024];
            while (digestInputStream.read(buffer) > -1) {}
            return digestInputStream.getMessageDigest().digest();
        } catch (Exception e) {
            throw new YopClientException(
                    "Unable to compute hash while signing request: " + e.getMessage(), e);
        }
    }

    protected static Map<String, MessageDigest> initMdInstance() {
        try {
            Map<String, MessageDigest> algMap = Maps.newHashMap();
            algMap.put("SM3", MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME));
            return algMap;
        } catch (Exception e) {
            LOGGER.warn("Unable to get Digest Function, will fail when use YopSm2Signer for sign, ex", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Returns the re-usable thread local version of MessageDigest.
     *
     * @return 摘要
     */
    private MessageDigest getMessageDigestInstance(String digestAlg) {
        MessageDigest messageDigest = MESSAGE_DIGEST.get().get(digestAlg);
        messageDigest.reset();
        return messageDigest;
    }
}
