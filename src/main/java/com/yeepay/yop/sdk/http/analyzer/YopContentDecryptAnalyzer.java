package com.yeepay.yop.sdk.http.analyzer;

import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseAnalyzer;
import com.yeepay.g3.core.yop.sdk.sample.http.HttpResponseHandleContext;
import com.yeepay.g3.core.yop.sdk.sample.http.YopHttpResponse;
import com.yeepay.g3.core.yop.sdk.sample.model.BaseResponse;
import com.yeepay.g3.core.yop.sdk.sample.model.YopResponseMetadata;
import com.yeepay.g3.core.yop.sdk.sample.utils.CharacterConstants;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * title: 结果解密<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
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

    private static final String CONTENT_TYPE_JSON = "application/json";

    private YopContentDecryptAnalyzer() {
    }

    @Override
    public <T extends BaseResponse> boolean analysis(HttpResponseHandleContext context, T response) throws Exception {
        YopResponseMetadata metadata = response.getMetadata();
        YopHttpResponse httpResponse = context.getResponse();
        if (BooleanUtils.isTrue(context.isNeedDecrypt()) && isJsonResponse(metadata.getContentType())) {
            String content = httpResponse.readContent();
            if (content != null && !StringUtils.startsWith(content, CharacterConstants.LEFT_BRACE)) {
                String decryptedContent = context.getEncryptor().decrypt(content);
                httpResponse.setDecryptedContent(decryptedContent);
            }
        }
        return false;
    }

    private boolean isJsonResponse(String contentType) {
        return StringUtils.startsWith(contentType, CONTENT_TYPE_JSON);
    }
}
