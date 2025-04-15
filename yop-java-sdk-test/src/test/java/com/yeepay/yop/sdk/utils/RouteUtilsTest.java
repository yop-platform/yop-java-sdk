/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.client.router.RouteUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2025/4/14
 */
public class RouteUtilsTest {

    @Test
    public void testWeightRoute() throws Exception {
        ArrayList<RouteUtils.WeightAble<Object>> origin = new ArrayList<>();
        origin.add(new RouteUtils.WeightAble<>("a", 30));
        origin.add(new RouteUtils.WeightAble<>("b", 30));
        origin.add(new RouteUtils.WeightAble<>("c", 40));
        int sum = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            if (RouteUtils.weightList(origin).get(0).getT().equals("b")) {
                sum ++;
            }
        }

        BigDecimal numerator = new BigDecimal(sum);
        BigDecimal denominator = new BigDecimal(total);

        // 使用 setScale 来保留两位小数，并选择舍入模式
        BigDecimal result = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        // 断言结果在 0.28 和 0.32 之间
        System.out.println(result);

        assert new BigDecimal("0.28").compareTo(result) < 0 && result.compareTo(new BigDecimal("0.32")) < 0;
    }

    @Test
    public void testIllegalWeightConfig() throws Exception {
        ArrayList<RouteUtils.WeightAble<Object>> origin = new ArrayList<>();
        origin.add(new RouteUtils.WeightAble<>("a", -30));
        origin.add(new RouteUtils.WeightAble<>("b", 30));
        origin.add(new RouteUtils.WeightAble<>("c", -40));
        int sum = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            if (RouteUtils.weightList(origin).get(0).getT().equals("b")) {
                sum ++;
            }
        }

        BigDecimal numerator = new BigDecimal(sum);
        BigDecimal denominator = new BigDecimal(total);

        // 使用 setScale 来保留两位小数，并选择舍入模式
        BigDecimal result = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        System.out.println(result);

        assert new BigDecimal("1.00").compareTo(result) == 0;
    }

    @Test
    public void testSingleWeightConfig() throws Exception {
        ArrayList<RouteUtils.WeightAble<Object>> origin = new ArrayList<>();
        origin.add(new RouteUtils.WeightAble<>("b", 30));
        int sum = 0;
        int total = 10000;
        for (int i = 0; i < total; i++) {
            if (RouteUtils.weightList(origin).get(0).getT().equals("b")) {
                sum ++;
            }
        }

        BigDecimal numerator = new BigDecimal(sum);
        BigDecimal denominator = new BigDecimal(total);

        // 使用 setScale 来保留两位小数，并选择舍入模式
        BigDecimal result = numerator.divide(denominator, 2, RoundingMode.HALF_UP);
        System.out.println(result);

        assert new BigDecimal("1.00").compareTo(result) == 0;
    }
}
