/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.auth.credentials.YopCredentials;
import com.yeepay.yop.sdk.auth.credentials.YopSymmetricCredentials;
import com.yeepay.yop.sdk.exception.YopServiceException;
import com.yeepay.yop.sdk.security.encrypt.BigParamEncryptMode;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yeepay.yop.sdk.YopConstants.YOP_ENCRYPT_V1;
import static com.yeepay.yop.sdk.constants.CharacterConstants.*;

/**
 * title: YOP加密协议<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public enum YopEncryptProtocol {

    /**
     * 加密协议头(请求)：yop-encrypt-v1/{服务端证书序列号}/{密钥类型(必填)}_{分组模式(必填)}_{填充算法(必填)}/{加密密钥值(必填)}/{IV}{;}{附加信息}/{客户端支持的大参数加密模式(必
     * 填)}/{encryptHeaders}/{encryptParams}
     */
    YOP_ENCRYPT_PROTOCOL_V1_REQ(YOP_ENCRYPT_V1) {
        @Override
        public Inst parse(ParseParams parseParams) {
            EncryptOptions srcEncryptOptions = parseParams.getSrcEncryptOptions();
            EncryptOptions parsedEncryptOptions = srcEncryptOptions.copy();
            String[] items = StringUtils.splitPreserveAllTokens(parseParams.getYopEncrypt(), SLASH);
            parseBasic(items, parsedEncryptOptions);
            parseCredentials(items, parsedEncryptOptions, parseParams.getYopCredentials());
            String encryptHeaderStr = items[6];
            String encryptParamStr = items[7];
            return new Inst(parsedEncryptOptions, parseEncryptItems(encryptHeaderStr, false),
                    parseEncryptItems(encryptParamStr, true));
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(YopEncryptProtocol.class);

    private String protocolPrefix;

    private static final Map<String, YopEncryptProtocol> protocolMap = Maps.newHashMap();

    static {
        for (YopEncryptProtocol protocol : YopEncryptProtocol.values()) {
            protocolMap.put(protocol.protocolPrefix, protocol);
        }
    }

    YopEncryptProtocol(String protocolPrefix) {
        this.protocolPrefix = protocolPrefix;
    }

    public static Set<String> parseEncryptItems(String itemStr, boolean base64) {
        try {
            if (StringUtils.isNotBlank(itemStr)) {
                String itemStrDecoded;
                // 兼容网关未encode场景
                if (base64 && !itemStr.equals(DOLLAR)) {
                    itemStrDecoded = new String(Encodes.decodeBase64(itemStr), YopConstants.DEFAULT_ENCODING);
                } else {
                    itemStrDecoded = itemStr;
                }
                return Arrays.stream(itemStrDecoded.split(SEMICOLON)).collect(Collectors.toSet());
            }
            return Collections.emptySet();
        } catch (UnsupportedEncodingException e) {
            throw new YopServiceException("error when parse encrypt protocol, ex:", e);
        }
    }

    public static YopEncryptProtocol fromProtocol(String protocol) {
        return protocolMap.get(StringUtils.substringBefore(protocol, SLASH));
    }

    public static YopEncryptProtocol fromProtocolPrefix(String protocolPrefix) {
        return protocolMap.get(protocolPrefix);
    }

    public String getProtocolPrefix() {
        return protocolPrefix;
    }

    abstract public Inst parse(ParseParams parseParams);

    private static void parseBasic(String[] items, EncryptOptions parsedEncryptOptions) {
        if (StringUtils.isNotBlank(items[4])) {
            String[] iv_AAD = StringUtils.splitPreserveAllTokens(items[4], SEMICOLON);
            parsedEncryptOptions.setIv(iv_AAD[0]);
            if (iv_AAD.length > 1) {
                parsedEncryptOptions.setAad(iv_AAD[1]);
            }
        }
        if (StringUtils.isNotBlank(items[2])) {
            parsedEncryptOptions.setAlg(StringUtils.replace(items[2], UNDER_LINE, SLASH));
        }

        if (StringUtils.isNotBlank(items[5])) {
            parsedEncryptOptions.setBigParamEncryptMode(BigParamEncryptMode.valueOf(items[5]));
        }
    }

    private static void parseCredentials(String[] items, EncryptOptions parsedEncryptOptions, YopCredentials<?> yopCredentials) {
        Object credential = parsedEncryptOptions.getCredentials();
        String credentialsAlg = parsedEncryptOptions.getCredentialsAlg();
        if (StringUtils.isNotBlank(items[3]) && credential instanceof YopSymmetricCredentials) {
            String encryptedCredentialStr = items[3];
            parsedEncryptOptions.setEncryptedCredentials(encryptedCredentialStr);

            try {
                byte[] encryptedCredentialBytes = Encodes.decodeBase64(encryptedCredentialStr);
                byte[] decryptedSecretKey = YopEncryptorFactory.getEncryptor(credentialsAlg)
                        .decrypt(encryptedCredentialBytes, new EncryptOptions(yopCredentials));
                if (null != decryptedSecretKey) {
                    credential = new YopSymmetricCredentials(yopCredentials.getAppKey(), Encodes.encodeUrlSafeBase64(decryptedSecretKey));
                    parsedEncryptOptions.setCredentials(credential);
                }
            } catch (Exception e) {
                LOGGER.warn("error when decrypt work credential with appKey:" + yopCredentials.getAppKey() + ", ex:", e);
            }
        }
    }

    /**
     * 一个解析后的协议实例
     */
    public static class Inst {
        private EncryptOptions encryptOptions;
        private Set<String> encryptHeaders;
        private Set<String> encryptParams;

        public Inst(EncryptOptions encryptOptions) {
            this.encryptOptions = encryptOptions;
        }

        public Inst(EncryptOptions encryptOptions, Set<String> encryptHeaders, Set<String> encryptParams) {
            this.encryptOptions = encryptOptions;
            this.encryptHeaders = encryptHeaders;
            this.encryptParams = encryptParams;
        }

        public EncryptOptions getEncryptOptions() {
            return encryptOptions;
        }

        public Set<String> getEncryptHeaders() {
            return encryptHeaders;
        }

        public Set<String> getEncryptParams() {
            return encryptParams;
        }
    }

    public static class ParseParams {
        /**
         * 加密头
         */
        private String yopEncrypt;

        /**
         * YOP商户凭证
         */
        private YopCredentials<?> yopCredentials;

        /**
         * 源加密选项
         */
        private EncryptOptions srcEncryptOptions;

        public ParseParams(String yopEncrypt, YopCredentials<?> yopCredentials, EncryptOptions srcEncryptOptions) {
            this.yopEncrypt = yopEncrypt;
            this.yopCredentials = yopCredentials;
            this.srcEncryptOptions = srcEncryptOptions;
        }

        public String getYopEncrypt() {
            return yopEncrypt;
        }

        public YopCredentials<?> getYopCredentials() {
            return yopCredentials;
        }

        public EncryptOptions getSrcEncryptOptions() {
            return srcEncryptOptions;
        }
    }
}
