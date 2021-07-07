/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer.process;

import java.util.HashMap;
import java.util.Map;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/7/6 下午6:57
 */
public class YopSignProcessFactory {
    private static Map<String, YopSignProcess> yopSignProcessMap = new HashMap(3) {
        {
            put("SM2", new YopSm2SignProcess());
            put("RSA2048", new YopRsaSignProcess());
        }
    };

    public static YopSignProcess getYopSignProcess(String certType) {
        return yopSignProcessMap.get(certType);
    }

    public static void registeYopSignProcess(String certType, YopSignProcess yopSignProcess) {
        yopSignProcessMap.put(certType, yopSignProcess);
    }
}
