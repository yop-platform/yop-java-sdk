package com.yeepay.g3.sdk.yop.unmarshaller;

import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * title: 通道规则 Service<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 15/11/19 20:00
 */
public class JacksonJsonMarshallerTest {

//    @Test
//    public void testMarshal() throws Exception {
//        IntStream s = IntStream.range(0, 15);
//        s.parallel().forEach(j ->  {
//            YopRequest request = new YopRequest(null, "8451VK46H9k50ZWF7mD1sMLR8ka2x0z8j72sJS708RWZ93uP4uR8b55r8Xn2",
//                    "https://open.yeepay.com/yop-center");
////			Map<String, Object> paramMap = JSON.parseObject("{\"requestId\":\"1447818443457103gsYp0926032\",\"customerNo\":\"10000446816\",\"merchantNo\":\"10012624945\"}");
////			Iterator<Entry<String, Object>> iter = paramMap.entrySet().iterator();
////			while (iter.hasNext()) {
////				Entry<String, Object> entry = iter.next();
////				String key = entry.getKey();
////				Object val = entry.getValue();
////				request.addParam(key, val);
////			}
//            request.addParam("requestId", "1447818443457103gsYp0926032");
//            request.addParam("customerNo", "10000446816");
//            request.addParam("merchantNo", "10012624945");
//
//            YopResponse response = null;
////			for (int i = 1; i <= 3; i++) {
////				try {
//            response = YopClient.post("/rest/v1.0/merchant/queryPayOrder", request);
//            System.out.println("1111111111111111111"+response.toString());
////				} catch (Exception e) {
////					e.printStackTrace();
////				}
////			}
//        });
//    }

    @Test
    public void testUnmarshal() throws Exception {
        int taskSize = 100;
        ExecutorService pool = Executors.newFixedThreadPool(taskSize);
        List<Future> list = new ArrayList<Future>();
        for (int i = 0; i < taskSize; i++) {
            Callable c = new MyCallable();
            Future f = pool.submit(c);
            list.add(f);
        }
        // 关闭线程池
        pool.shutdown();

        // 获取所有并发任务的运行结果
        for (Future f : list) {
            // 从Future对象上获取任务的返回值，并输出到控制台
            System.out.println(">>>" + f.get().toString());
        }
    }

    class MyCallable implements Callable<Object> {

        public Object call() throws Exception {
//            YopRequest request = new YopRequest(null, "8451VK46H9k50ZWF7mD1sMLR8ka2x0z8j72sJS708RWZ93uP4uR8b55r8Xn2",
//                    "https://open.yeepay.com/yop-center");
//            request.addParam("requestId", "1447818443457103gsYp0926032");
//            request.addParam("customerNo", "10000446816");
//            request.addParam("merchantNo", "10012624945");

//            String response = YopClient.post("/rest/v1.0/merchant/queryPayOrder", request).toString();

            YopRequest request = new YopRequest("TestAppKey002","Zj4xyBkgjd");
            request.setSignAlg("SHA1");
//            request.setSignAlg("MD5");//具体看api签名算法而定
            String notifyRule = "fundauth_MOBILE_IFVerify111";//通知规则
            List recipients = new ArrayList();//接收人
            recipients.add(0, "18511620061");
            String content = "{code:12345,something:something}";//json字符串，code，mctName为消息模板变量
            String extNum = "01";//扩展码
            String feeSubject = "0.01";//计费主体
            request.addParam("notifyRule", notifyRule);
            request.addParam("recipients", recipients);
            request.addParam("content", content);
            request.addParam("extNum", extNum);
            request.addParam("feeSubject", feeSubject);
            YopResponse response = YopClient.post("/rest/v1.0/notifier/send", request);
//            System.out.println(response);
            return response;
        }

    }

    @Test
    public void test2(){
        String responseStr = "{\n" +
                "  \"state\" : \"SUCCESS\",\n" +
                "  \"result\" : \"123\",\n" +
                "  \"ts\" : 1528101318717\n" +
                "}";
        YopResponse response = JacksonJsonMarshaller.unmarshal(responseStr, YopResponse.class);
        response.setResult(JacksonJsonMarshaller.unmarshal(response.getStringResult(), Object.class));
        System.out.println(response);
    }

    @Test
    public void test3() {
        String responseStr = "{\n" +
                "  \"state\" : \"SUCCESS\",\n" +
                "  \"result\" : true,\n" +
                "  \"ts\" : 1528101318717\n" +
                "}";
        YopResponse response = JacksonJsonMarshaller.unmarshal(responseStr, YopResponse.class);
        response.setResult(JacksonJsonMarshaller.unmarshal(response.getStringResult(), Object.class));
        System.out.println(response);
    }

    @Test
    public void test4() {
        String responseStr = "{\n" +
                "  \"state\" : \"SUCCESS\",\n" +
                "  \"result\" : {\"name\":\"xx\"},\n" +
                "  \"ts\" : 1528101318717\n" +
                "}";
        YopResponse response = JacksonJsonMarshaller.unmarshal(responseStr, YopResponse.class);
        response.setResult(JacksonJsonMarshaller.unmarshal(response.getStringResult(), Object.class));
        System.out.println(response);
    }

}