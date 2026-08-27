/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils.json;

import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * title: 原始报文反序列化测试<br>
 * description: 覆盖jackson对超长json包装StringReader的场景，需在JDK8、11、17、21、25下均通过<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2026/8/27
 */
public class KeepAsRawStringDeserializerTest {

    /**
     * jackson对超过该长度的json会包装为StringReader，取原始报文的分支不同
     */
    private static final int STRING_READER_THRESHOLD = 0x8000;

    private static final String RESULT_OBJECT =
            "{\"code\":\"OPR00000\",\"amount\":1.10,\"count\":12345678901234567890123,\"remark\":\"中文\\\"引号\\\\转义\",\"details\":[1,2,{\"sub\":null}]}";

    private static final String RESULT_ARRAY = "[{\"amount\":1.10},{\"amount\":0.00}]";

    @Test
    public void testSmallJsonObject() throws IOException {
        final String content = buildContent(RESULT_OBJECT, 16);
        Assert.assertTrue(content.length() <= STRING_READER_THRESHOLD);
        Assert.assertEquals(RESULT_OBJECT, load(content).getStringResult());
    }

    @Test
    public void testLargeJsonObject() throws IOException {
        final String content = buildContent(RESULT_OBJECT, STRING_READER_THRESHOLD);
        Assert.assertTrue(content.length() > STRING_READER_THRESHOLD);
        Assert.assertEquals(RESULT_OBJECT, load(content).getStringResult());
    }

    @Test
    public void testLargeJsonArray() throws IOException {
        final String content = buildContent(RESULT_ARRAY, STRING_READER_THRESHOLD);
        Assert.assertEquals(RESULT_ARRAY, load(content).getStringResult());
    }

    @Test
    public void testLargeJsonWithLargeResult() throws IOException {
        final StringBuilder largeResult = new StringBuilder("{\"records\":[");
        for (int i = 0; i < 2000; i++) {
            largeResult.append(i > 0 ? "," : "").append("{\"no\":").append(i).append(",\"amount\":1.10}");
        }
        largeResult.append("]}");
        final String result = largeResult.toString();
        final String content = buildContent(result, 0);
        Assert.assertTrue(content.length() > STRING_READER_THRESHOLD);
        Assert.assertEquals(result, load(content).getStringResult());
    }

    @Test
    public void testLargeJsonSimpleTypes() throws IOException {
        Assert.assertEquals("\"SUCCESS\"", load(buildContent("\"SUCCESS\"", STRING_READER_THRESHOLD)).getStringResult());
        Assert.assertEquals("1.10", load(buildContent("1.10", STRING_READER_THRESHOLD)).getStringResult());
        Assert.assertEquals("true", load(buildContent("true", STRING_READER_THRESHOLD)).getStringResult());
    }

    /**
     * 外部直接使用ObjectMapper时拿不到透传的原始报文，低版本JDK走反射、高版本JDK走重建，二者结果需语义一致
     */
    @Test
    public void testLargeJsonWithoutRawJsonAttribute() throws IOException {
        final String content = buildContent(RESULT_OBJECT, STRING_READER_THRESHOLD);

        final YopResponse response = new YopResponse();
        JsonUtils.getObjectMapper().readerForUpdating(response).readValue(content);

        // 小数末尾零、超长整数均不可丢失，否则与透传原始报文的结果不等
        Assert.assertTrue(response.getStringResult().contains("1.10"));
        Assert.assertTrue(response.getStringResult().contains("12345678901234567890123"));
        Assert.assertEquals(load(content).getResult(), response.getResult());
    }

    private YopResponse load(String content) throws IOException {
        final YopResponse response = new YopResponse();
        JsonUtils.load(content, response);
        return response;
    }

    private String buildContent(String result, int padLength) {
        final StringBuilder content = new StringBuilder("{\"state\":\"SUCCESS\",\"result\":")
                .append(result).append(",\"pad\":\"");
        for (int i = 0; i < padLength; i++) {
            content.append('x');
        }
        return content.append("\"}").toString();
    }
}
