package com.yeepay.yop.sdk.protocol.marshaller;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.protocol.marshaller.support.DateTimeMarshaller;
import com.yeepay.yop.sdk.protocol.marshaller.support.LocalDateMarshaller;

import java.util.Map;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/23 18:40
 */
public class PrimitiveMarshallerUtils {

    private static final Map<String, PrimitiveMarshaller> MARSHALLERS = Maps.newHashMap();

    static {
        register(new LocalDateMarshaller());
        register(new DateTimeMarshaller());
    }

    public static <T> String marshalling(T param, String type) {
        if (param == null) {
            return null;
        }
        PrimitiveMarshaller marshaller = MARSHALLERS.get(type);
        if (marshaller == null) {
            return param.toString();
        }
        return marshaller.marshalling(param);
    }

    private static void register(PrimitiveMarshaller marshaller) {
        MARSHALLERS.put(marshaller.supportedType(), marshaller);
    }
}
