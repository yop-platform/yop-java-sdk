package com.yeepay.yop.sdk.protocol.marshaller.support;

import com.yeepay.yop.sdk.protocol.marshaller.PrimitiveMarshaller;
import com.yeepay.yop.sdk.utils.DateUtils;
import org.joda.time.DateTime;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/29 18:25
 */
public class DateTimeMarshaller implements PrimitiveMarshaller {
    @Override
    public String supportedType() {
        return "DateTime";
    }

    @Override
    public <T> String marshalling(T param) {
        return DateUtils.formatSimpleDateTime((DateTime) param);
    }
}
