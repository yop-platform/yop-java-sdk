package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.yop.sdk.http.HttpResponseAnalyzer;
import com.yeepay.yop.sdk.http.HttpResponseHandleContext;
import com.yeepay.yop.sdk.http.YopHttpResponse;
import com.yeepay.yop.sdk.model.BaseResponse;
import com.yeepay.yop.sdk.model.YopResponseMetadata;
import com.yeepay.yop.sdk.utils.CharacterConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import static com.yeepay.yop.sdk.utils.HttpUtils.isJsonContent;

/**
 * title: 结果解密<br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-30 14:39
 */
public class YopContentDecryptAnalyzer implements HttpResponseAnalyzer {

    private static final YopContentDecryptAnalyzer INSTANCE = new YopContentDecryptAnalyzer();

    public static YopContentDecryptAnalyzer getInstance() {
        return INSTANCE;
    }

    private YopContentDecryptAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopResponseMetadata metadata = response.getMetadata();
        YopHttpResponse httpResponse = context.getResponse();
        if (BooleanUtils.isTrue(context.isNeedDecrypt()) && isJsonContent(metadata.getContentType())) {
            String content = httpResponse.readContent();
            if (content != null && !StringUtils.startsWith(content, CharacterConstants.LEFT_BRACE)) {
                String decryptedContent = context.getEncryptor().decrypt(content);
                httpResponse.setDecryptedContent(decryptedContent);
            }
        }
        return false;
    }
}
