//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yeepay.yop.sdk.security;

import com.google.common.collect.Maps;

import java.util.Map;

public enum SymmetricEncryptAlgEnum {
    AES("AES", "aes加密算法"),
    SM4("SM4", "sm4加密算法");

    private static final Map<String, SymmetricEncryptAlgEnum> VALUE_MAP = Maps.newHashMap();
    private final String value;
    private final String displayName;

    SymmetricEncryptAlgEnum(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static SymmetricEncryptAlgEnum parse(String value) {
        return VALUE_MAP.get(value);
    }

    public String getValue() {
        return this.value;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static Map<String, SymmetricEncryptAlgEnum> getValueMap() {
        return VALUE_MAP;
    }

    static {
        SymmetricEncryptAlgEnum[] var0 = values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            SymmetricEncryptAlgEnum item = var0[var2];
            VALUE_MAP.put(item.value, item);
        }

    }
}
