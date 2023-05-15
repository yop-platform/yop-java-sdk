package com.yeepay.yop.sdk.auth.cipher;

import com.yeepay.g3.core.yop.sdk.sample.YopConstants;
import com.yeepay.g3.core.yop.sdk.sample.auth.Encryptor;
import com.yeepay.g3.core.yop.sdk.sample.auth.credentials.YopRSACredentials;
import com.yeepay.g3.core.yop.sdk.sample.exception.YopClientException;
import com.yeepay.g3.core.yop.sdk.sample.http.Headers;
import com.yeepay.g3.core.yop.sdk.sample.internal.Request;
import com.yeepay.g3.core.yop.sdk.sample.internal.RestartableInputStream;
import com.yeepay.g3.core.yop.sdk.sample.security.aes.AesEncryptor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * title: 默认加密器<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2019<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2019-04-25 11:07
 */
public class DefaultEncryptor implements Encryptor {

    private static final String SINGLE_FILE_UPLOAD_CONTENT_TYPE = "application/octet-stream";

    private final byte[] key;

    public DefaultEncryptor(YopRSACredentials credentials) {
        String secretKey = credentials.getEncryptKey();
        if (StringUtils.isEmpty(secretKey)) {
            throw new YopClientException("no encryptKey configured");
        }
        byte[] key = Base64.decodeBase64(secretKey.getBytes(YopConstants.DEFAULT_CHARSET));
        if (key.length != 16 && key.length != 32) {
            throw new YopClientException("unsupported encryptKey length");
        }
        this.key = key;
    }

    @Override
    public void encrypt(Request request) {
        if (isSingleFileUpload(request)) {
            return;
        }
        request.addHeader(Headers.YOP_ENCRYPT_TYPE, "aes" + key.length * 8);
        if (MapUtils.isNotEmpty(request.getParameters())) {
            Map<String, List<String>> parameters = request.getParameters();
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                List<String> encryptedValues = new ArrayList<String>(entry.getValue().size());
                for (String value : entry.getValue()) {
                    encryptedValues.add(new String(Base64.encodeBase64(AesEncryptor.encrypt(value.getBytes(YopConstants.DEFAULT_CHARSET), key)),
                            YopConstants.DEFAULT_CHARSET));
                }
                entry.setValue(encryptedValues);
            }
        }
        if (request.getContent() != null) {
            try {
                byte[] content = IOUtils.toByteArray(request.getContent());
                request.setContent(RestartableInputStream.wrap(AesEncryptor.encrypt(content, key)));
            } catch (IOException ex) {
                throw new YopClientException("IoException occurred when encrypt content", ex);
            } catch (Throwable ex) {
                throw new YopClientException("UnExpectedException occurred when encrypt content", ex);
            }
        }

    }

    @Override
    public String decrypt(String content) {
        return new String(AesEncryptor.decrypt(Base64.decodeBase64(content.getBytes(YopConstants.DEFAULT_CHARSET)), key),
                YopConstants.DEFAULT_CHARSET);
    }

    private boolean isSingleFileUpload(Request request) {
        return StringUtils.equals((String) request.getHeaders().get(Headers.CONTENT_TYPE), SINGLE_FILE_UPLOAD_CONTENT_TYPE);
    }
}
