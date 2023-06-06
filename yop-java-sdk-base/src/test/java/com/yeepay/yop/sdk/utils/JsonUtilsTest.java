/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.client.metric.report.api.YopFailDetail;
import com.yeepay.yop.sdk.client.metric.report.api.YopHostRequestPayload;
import com.yeepay.yop.sdk.client.metric.report.api.YopHostRequestReport;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/6/6
 */
public class JsonUtilsTest {

    @Test
    public void testReport() throws IOException {
        final YopHostRequestReport loaded = new YopHostRequestReport();
        final String repostStr = "{\"type\":\"YopHostRequestReport\",\"version\":1,\"payload\":{\"successCount\":0,\"failCount\":0,\"maxElapsedMillis\":0,\"failDetails\":[{\"exType\":\"\",\"exMsg\":\"\",\"occurTime\":[\"2023-06-06 08:15:43\"]}]},\"beginTime\":\"2023-06-06 08:15:43\"}";
        JsonUtils.load(repostStr, loaded);

        final YopHostRequestReport newObj = new YopHostRequestReport();
        newObj.setBeginTime(new Date(1686039343000L));
        final YopHostRequestPayload payload = new YopHostRequestPayload();
        payload.setFailDetails(Lists.newArrayList(new YopFailDetail("","", Lists.newArrayList(new Date(1686039343000L)))));
        newObj.setPayload(payload);

        final String newObjStr = JsonUtils.toJsonString(newObj);

        Assert.assertEquals(repostStr, newObjStr);
    }
}
