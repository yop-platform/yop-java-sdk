//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yeepay.yop.sdk.security;

import java.util.HashMap;
import java.util.Map;

public enum DigestAlgEnum {
    SHA256("SHA256", "sha-256摘要"),
    SHA512("SHA512", "sha-512摘要");

    private static final Map<String, DigestAlgEnum> VALUE_MAP = new HashMap();
    private String value;
    private String displayName;

    private DigestAlgEnum(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static DigestAlgEnum parse(String value) {
        return (DigestAlgEnum)VALUE_MAP.get(value);
    }

    public String getValue() {
        return this.value;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static Map<String, DigestAlgEnum> getValueMap() {
        return VALUE_MAP;
    }

    static {
        DigestAlgEnum[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            DigestAlgEnum item = var0[var2];
            VALUE_MAP.put(item.value, item);
        }

    }
}
