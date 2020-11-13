package com.yeepay.yop.sdk.protocol.marshaller;

/**
 * title: <br>
 * description: <br>
 * Copyright: Copyright (c) 2018<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/3/27 16:56
 */
public interface PrimitiveMarshaller {

    /**
     * 支持的类型
     *
     * @return 支持的类型
     */
    String supportedType();

    /**
     * @param param
     * @param <T>
     * @return
     */
    <T> String marshalling(T param);

}