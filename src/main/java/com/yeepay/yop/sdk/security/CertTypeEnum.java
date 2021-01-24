/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security;

import java.util.HashMap;
import java.util.Map;

public enum CertTypeEnum {
    AES128("AES128", "AES算法，密钥长度128", "AES", 128, 16, true),
    AES256("AES256", "AES算法, 密钥长度256", "AES", 256, 32, true),
    RSA2048("RSA2048", "RSA算法, 密钥长度2048", "RSA", 2048, 294, false),
    SM2("SM2", "SM2算法，密钥长度256", "SM2", 256, 32, false);

    private static final Map<String, CertTypeEnum> VALUE_MAP = new HashMap<String, CertTypeEnum>();

    private final String value;

    private final String displayName;

    private final String algorithm;

    private final int keySize;

    private final int encodedSize;

    private final boolean symmetric;

    static {
        for (CertTypeEnum item : CertTypeEnum.values()) {
            VALUE_MAP.put(item.value, item);
        }
    }

    CertTypeEnum(String value, String displayName, String algorithm, int keySize, int encodedSize, boolean symmetric) {
        this.value = value;
        this.displayName = displayName;
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.symmetric = symmetric;
        this.encodedSize = encodedSize;
    }

    public static CertTypeEnum parse(String value) {
        return VALUE_MAP.get(value);
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public int getKeySize() {
        return keySize;
    }

    public boolean isSymmetric() {
        return symmetric;
    }

    public int getEncodedSize() {
        return encodedSize;
    }

    public static Map<String, CertTypeEnum> getValueMap() {
        return VALUE_MAP;
    }


}