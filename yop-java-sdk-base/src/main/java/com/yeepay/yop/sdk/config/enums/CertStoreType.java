package com.yeepay.yop.sdk.config.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/12/27 上午9:10
 */
public enum CertStoreType {

    STRING("string", "明文"),
    FILE_CER("file_cer", "公钥证书文件"),
    FILE_P12("file_p12", "文件");

    private static final Map<String, CertStoreType> VALUE_MAP = Maps.newHashMap();

    private final String value;
    private final String displayName;

    static {
        for (CertStoreType item : CertStoreType.values()) {
            VALUE_MAP.put(item.value, item);
        }
    }

    CertStoreType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @JsonCreator
    public static CertStoreType parse(String value) {
        return VALUE_MAP.get(value);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Map<String, CertStoreType> getValueMap() {
        return VALUE_MAP;
    }
}
