/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.encrypt;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yeepay.yop.sdk.YopConstants;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptions;
import com.yeepay.yop.sdk.security.encrypt.EncryptOptionsEnhancer;
import com.yeepay.yop.sdk.security.encrypt.YopEncryptor;
import com.yeepay.yop.sdk.utils.Encodes;
import org.apache.commons.collections4.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * title: 加解密适配器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public abstract class YopEncryptorAdaptor implements YopEncryptor {

    protected static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(2, 20,
            3, TimeUnit.MINUTES, Queues.newLinkedBlockingQueue(200),
            new ThreadFactoryBuilder().setNameFormat("yop-encryptor-task-%d").setDaemon(true).build(), new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public Future<EncryptOptions> initOptions(String encryptAlg, List<EncryptOptionsEnhancer> enhancers) {
        return THREAD_POOL.submit(new InitOptionsTask(encryptAlg, enhancers));
    }

    @Override
    public String encryptToBase64(String plain, EncryptOptions options) {
        try {
            return Encodes.encodeUrlSafeBase64(encrypt(plain.getBytes(YopConstants.DEFAULT_ENCODING), options));
        } catch (UnsupportedEncodingException e) {
            throw new YopClientException("error happened when encrypt data", e);
        }
    }

    @Override
    public String encryptToBase64(byte[] plain, EncryptOptions options) {
        return Encodes.encodeUrlSafeBase64(encrypt(plain, options));
    }

    @Override
    public String decryptFromBase64(String cipher, EncryptOptions options) {
        return decryptToPlain(Encodes.decodeBase64(cipher), options);
    }

    @Override
    public String decryptToPlain(byte[] cipher, EncryptOptions options) {
        try {
            return new String(decrypt(cipher, options), YopConstants.DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new YopClientException("error happened when decrypt data", e);
        }
    }

    /**
     * 初始化加密选项
     *
     * @param encryptAlg 加密算法
     * @return EncryptOptions
     */
    public abstract EncryptOptions doInitEncryptOptions(String encryptAlg) throws Exception;

    private class InitOptionsTask implements Callable<EncryptOptions> {

        private final String encryptAlg;
        private final List<EncryptOptionsEnhancer> enhancers;

        public InitOptionsTask(String encryptAlg, List<EncryptOptionsEnhancer> enhancers) {
            this.encryptAlg = encryptAlg;
            this.enhancers = enhancers;
        }

        @Override
        public EncryptOptions call() throws Exception {
            EncryptOptions inited = doInitEncryptOptions(encryptAlg);
            if (CollectionUtils.isNotEmpty(enhancers)) {
                for (EncryptOptionsEnhancer enhancer : enhancers) {
                    inited = enhancer.enhance(inited);
                }
            }
            return inited;
        }
    }
}
