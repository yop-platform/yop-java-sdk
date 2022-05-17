package com.yeepay.yop.sdk.protocol;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * title: 认证协议版本<br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 18/1/2 下午7:48
 */
public enum AuthenticateProtocolVersion {

    YOP_V2("yop-auth-v2", 2),
    YOP_V3("yop-auth-v3", 3);

    /**
     * string格式的认证协议版本，会在请求时在authorization header中传递
     */
    private String versionInStringFormat;

    /**
     * number格式的认证协议版本，用于进行版本比较
     */
    private int versionInNumberFormat;

    private static final Map<String, AuthenticateProtocolVersion> VALUE_MAP = Maps.newHashMap();

    static {
        for (AuthenticateProtocolVersion item : AuthenticateProtocolVersion.values()) {
            VALUE_MAP.put(item.versionInStringFormat, item);
        }
    }

    AuthenticateProtocolVersion(String versionInStringFormat, int versionInNumberFormat) {
        this.versionInStringFormat = versionInStringFormat;
        this.versionInNumberFormat = versionInNumberFormat;
    }

    public static AuthenticateProtocolVersion parse(String versionInStringFormat) {
        return VALUE_MAP.get(versionInStringFormat);
    }

    public String stringFormat() {
        return versionInStringFormat;
    }

    public Integer numberFormat() {
        return versionInNumberFormat;
    }
}
