/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.base.utils;

import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.utils.Encodes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * title: 国密初始化类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class SmUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmUtils.class);

    static {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            LOGGER.debug("BouncyCastleProvider added");
        } catch (Exception e) {
            LOGGER.warn("error when add BouncyCastleProvider", e);
        }
    }

    /**
     * 国密初始化
     */
    public static void init() {
        // already done when class loaded;
    }

    /**
     * string类型的公钥转换成公钥对象
     *
     * @param pubKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws NoSuchProviderException
     */
    public static PublicKey string2PublicKey(String pubKey) {
        try {
            byte[] x509Bytes = Encodes.decodeBase64(pubKey);
            X509EncodedKeySpec eks = new X509EncodedKeySpec(x509Bytes);
            KeyFactory kf = KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            return kf.generatePublic(eks);
        } catch (Exception e) {
            throw new YopClientException(e.getMessage());
        }
    }

}
