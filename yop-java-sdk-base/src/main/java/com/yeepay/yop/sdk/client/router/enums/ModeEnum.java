package com.yeepay.yop.sdk.client.router.enums;

/**
 * title: 运行模式<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-03-12 10:39
 */
public enum ModeEnum {
    /**
     * 生产模式
     */
    prod("prod", "生产"),

    /**
     * 沙箱模式
     */
    sandbox("sandbox", "沙箱");

    private final String code;

    private final String name;

    ModeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

}
