/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.security.encrypt;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Future;

/**
 * title: 加解密器 <br>
 * description: <br>
 * Copyright: Copyright (c) 2019<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/12
 */
public interface YopEncryptor {

    /**
     * 支持的算法列表
     * 单个算法的格式：{密钥类型}_{分组模式}_{填充模式}
     *
     * @return 算法列表
     */
    List<String> supportedAlgs();

    /**
     * 生成加解密选项（异步）
     *
     * @param encryptAlg 加解密算法
     * @param enhancers 加解密选项增强处理
     * @return 加解密选项(密钥+算法+iv+aad)
     */
    Future<EncryptOptions> initOptions(String encryptAlg, List<EncryptOptionsEnhancer> enhancers);

    /**
     * 加密普通参数
     *
     * @param plain 明文
     * @return 密文(base64编码)
     */
    String encryptToBase64(String plain, EncryptOptions options);

    /**
     * 加密字节数组
     *
     * @param plain 明文
     * @return 密文(base64编码)
     */
    String encryptToBase64(byte[] plain, EncryptOptions options);

    /**
     * 加密字节数组
     *
     * @param plain 明文
     * @return 密文
     */
    byte[] encrypt(byte[] plain, EncryptOptions options);

    /**
     * 加密数据流
     *
     * @param plain 明文
     * @return 密文
     */
    InputStream encrypt(InputStream plain, EncryptOptions options);

    /**
     * 解密普通参数
     *
     * @param cipher 密文(base64编码)
     * @return 明文字符串(默认UTF-8编码)
     */
    String decryptFromBase64(String cipher, EncryptOptions options);

    /**
     * 解密字节数组
     *
     * @param cipher 密文
     * @return 明文字符串(默认UTF-8编码)
     */
    String decryptToPlain(byte[] cipher, EncryptOptions options);

    /**
     * 解密字节数组
     *
     * @param cipher 密文
     * @return 明文
     */
    byte[] decrypt(byte[] cipher, EncryptOptions options);

    /**
     * 解密数据流
     *
     * @param cipher 密文
     * @return 明文
     */
    InputStream decrypt(InputStream cipher, EncryptOptions options);
}
