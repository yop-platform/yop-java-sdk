/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.router;

import com.yeepay.yop.sdk.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * title: 路由工具<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/3/31
 */
public class RouteUtils {

    public static <T> List<T> randomList(List<T> origin) {
        List<T> tmp = new ArrayList<>(origin);
        Collections.shuffle(tmp, RandomUtils.secureRandom());
        return tmp;
    }

    public static <T> T randomOne(List<T> origin) {
        return origin.get(RandomUtils.secureRandom().nextInt(origin.size()));
    }
}
