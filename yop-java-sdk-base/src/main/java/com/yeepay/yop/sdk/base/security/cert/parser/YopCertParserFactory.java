/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.base.security.cert.parser;

import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.exception.YopClientException;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.ServiceLoader;

/**
 * title: 密钥解析器工厂类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopCertParserFactory {

    /**
     * 解析器Map
     * <p>
     * key: 解析器ID
     * value: 解析器
     */
    private static final Map<String, YopCertParser> YOP_CERT_PARSER_MAP = Maps.newHashMap();

    static {
        ServiceLoader<YopCertParser> serviceLoader = ServiceLoader.load(YopCertParser.class);
        for (YopCertParser certParser : serviceLoader) {
            YOP_CERT_PARSER_MAP.put(certParser.parserId(), certParser);
        }
    }

    /**
     * 扩展解析器
     *
     * @param parserId   解析器ID
     * @param certParser 解析器
     */
    public static void registerParser(String parserId, YopCertParser certParser) {
        YOP_CERT_PARSER_MAP.put(parserId, certParser);
    }

    /**
     * 根据解析器ID获取解析器
     *
     * @param parserId 解析器ID
     * @return 解析器
     */
    public static YopCertParser getCertParser(String parserId) {
        final YopCertParser yopCertParser = YOP_CERT_PARSER_MAP.get(parserId);
        if (null == yopCertParser) {
            throw new YopClientException("YopCertParser not found, parserId:" + parserId);
        }
        return yopCertParser;
    }

    /**
     * 构造parserId
     *
     * @param certCategory 密钥类别
     * @param certType     密钥类型
     * @return String
     */
    public static String getParserId(YopCertCategory certCategory, CertTypeEnum certType) {
        return StringUtils.joinWith(CharacterConstants.COMMA, certCategory, certType);
    }

    /**
     * 根据密钥类别、类型获取解析器
     *
     * @param certCategory 密钥类别
     * @param certType     密钥类型
     * @return
     */
    public static YopCertParser getCertParser(YopCertCategory certCategory, CertTypeEnum certType) {
        return getCertParser(getParserId(certCategory, certType));
    }
}
