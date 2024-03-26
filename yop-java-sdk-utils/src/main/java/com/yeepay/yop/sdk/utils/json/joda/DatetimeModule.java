package com.yeepay.yop.sdk.utils.json.joda;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;

/**
 * title: DateTime模块<br>
 * description: <br>
 * Copyright: Copyright (c) 2020<br>
 * Company: 易宝支付(YeePay)<br>
 */
public class DatetimeModule extends SimpleModule {

    private static final long serialVersionUID = -1L;

    public DatetimeModule() {
        addSerializer(DateTime.class, new DateTimeSerializer());
        addDeserializer(DateTime.class, new DateTimeDeserializer());
    }
}
