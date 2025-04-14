/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.client.router;

import com.yeepay.yop.sdk.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

    public static <T> List<WeightAble<T>> weightList(List<WeightAble<T>> origin) {
        int total = origin
                .stream()
                .filter(d -> d.getWeight() > 0)
                .mapToInt(WeightAble::getWeight)
                .sum();
        if (total > 0) {
            int random = RandomUtils.secureRandom().nextInt(total);
            int accumulator = 0;
            List<WeightAble<T>> tmp = new ArrayList<>(origin);
            WeightAble<T> choosed = null;
            Iterator<WeightAble<T>> iterator = tmp.iterator();
            while (iterator.hasNext()) {
                WeightAble<T> dc = iterator.next();
                accumulator += dc.getWeight();
                if (random < accumulator) {
                    choosed = dc;
                    iterator.remove();
                    break;
                }
            }
            tmp.add(0, choosed);
            return tmp;
        }
        return randomList(origin);
    }

    public static class WeightAble<T> {
        private T t;
        private int weight;

        public WeightAble(T t, int weight) {
            this.t = t;
            this.weight = weight;
        }

        public T getT() {
            return t;
        }

        public void setT(T t) {
            this.t = t;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }
}
