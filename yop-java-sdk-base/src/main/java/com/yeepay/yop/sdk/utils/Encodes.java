package com.yeepay.yop.sdk.utils;

import com.yeepay.yop.sdk.exception.YopClientException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;

/**
 * title: 封装各种格式的编码解码工具类<br>
 * description: <br>
 * 1.Commons-Codec的 hex/base64 编码<br>
 * 2.自制的base62 编码<br>
 * 3.Commons-Lang的xml/html escape<br>
 * 4.JDK提供的URLEncoder<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/11/23 下午3:34
 */
public class Encodes {

    public static final String DEFAULT_URL_ENCODING = "UTF-8";
    private static final char[] BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int[] DEFAULT_CONFOUNDER = new int[]{3, 6, 7, 4, 1, 8, 0, 9, 5, 2};

    public static String confuse(long num) {
        String tempStr = Long.toString(num);
        int numLength = tempStr.length();
        char[] input = tempStr.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numLength; ++i) {
            sb.append(DEFAULT_CONFOUNDER[Integer.parseInt(input[i] + "")]);
        }
        return sb.toString();
    }

    /**
     * Hex编码.
     */
    public static String encodeHex(byte[] input) {
        return Hex.encodeHexString(input);
    }

    /**
     * Hex解码.
     */
    public static byte[] decodeHex(String input) {
        try {
            return Hex.decodeHex(input.toCharArray());
        } catch (DecoderException e) {
            throw new YopClientException("Decode Hex fails.", e);
        }
    }

    /**
     * Base64编码.
     */
    public static String encodeBase64(byte[] input) {
        return StringUtils.newStringUtf8(Base64.encodeBase64(input));
    }

    /**
     * Base64编码, URL安全(将Base64中的URL非法字符'+'和'/'转为'-'和'_', 见RFC3548).
     */
    public static String encodeUrlSafeBase64(byte[] input) {
        return Base64.encodeBase64URLSafeString(input);
    }

    /**
     * Base64解码.
     */
    public static byte[] decodeBase64(String input) {
        return Base64.decodeBase64(input);
    }

    /**
     * Base62编码。
     */
    public static String encodeBase62(byte[] input) {
        char[] chars = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            chars[i] = BASE62[(input[i] & 0xFF) % BASE62.length];
        }
        return new String(chars);
    }

    /**
     * Html 转码.
     */
    public static String escapeHtml(String html) {
        return StringEscapeUtils.escapeHtml4(html);
    }

    /**
     * Html 解码.
     */
    public static String unescapeHtml(String htmlEscaped) {
        return StringEscapeUtils.unescapeHtml4(htmlEscaped);
    }

    /**
     * Xml 转码.
     */
    public static String escapeXml(String xml) {
        return StringEscapeUtils.escapeXml(xml);
    }

    /**
     * Xml 解码.
     */
    public static String unescapeXml(String xmlEscaped) {
        return StringEscapeUtils.unescapeXml(xmlEscaped);
    }

    /**
     * URL 编码, Encode默认为UTF-8.
     */
    public static String urlEncode(String part) {
        try {
            return URLEncoder.encode(part, DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new YopClientException("Url encode fails.", e);
        }
    }

    /**
     * URL 解码, Encode默认为UTF-8.
     */
    public static String urlDecode(String part) {

        try {
            return URLDecoder.decode(part, DEFAULT_URL_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new YopClientException("Url Decode fails.", e);
        }
    }

    /**
     * 对密钥做base64编码
     *
     * @param key 密钥
     * @return
     */
    public static String encodeKey(Key key) {
        return encodeBase64(key.getEncoded());
    }
}
