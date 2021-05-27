package com.yeepay.yop.sdk.http.reselut;


import com.yeepay.yop.sdk.service.common.response.YopResponse;
import com.yeepay.yop.sdk.service.common.response.YosUploadResponse;
import org.junit.Assert;

public class AssertResponse {
    public static void assertResult( YopResponse response){
        Assert.assertNotNull("签名值不为空"+response.getMetadata().getYopSign());
        Assert.assertFalse("签名中有-是错的",response.getMetadata().getYopSign().contains("-"));
        Assert.assertFalse("签名中有_是错的",response.getMetadata().getYopSign().contains("_"));
        Assert.assertFalse("签名中有空格是错的",response.getMetadata().getYopSign().contains(" "));
    }

    public static void assertUpdateResult( YosUploadResponse upload ){
        Assert.assertNotNull("签名值不为空"+upload.getMetadata().getYopSign());
        Assert.assertFalse("签名中有-是错的",upload.getMetadata().getYopSign().contains("-"));
        Assert.assertFalse("签名中有_是错的",upload.getMetadata().getYopSign().contains("_"));
        Assert.assertFalse("签名中有空格是错的",upload.getMetadata().getYopSign().contains(" "));
    }
}
