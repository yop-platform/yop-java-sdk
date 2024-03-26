/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.utils;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.yeepay.yop.sdk.exception.YopClientException;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static com.yeepay.yop.sdk.YopConstants.*;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/3/21
 */
public class EncryptUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptUtils.class);

    public static boolean isTotalEncrypt(Set<String> jsonPaths) {
        boolean totalEncrypt = CollectionUtils.isSubCollection(jsonPaths, JSON_PATH_ROOT);
        if (totalEncrypt) {
            return true;
        }
        if (jsonPaths.size() > 1 && !CollectionUtils.intersection(jsonPaths, JSON_PATH_ROOT).isEmpty()) {
            throw new YopClientException("illegal json paths:" + jsonPaths);
        }
        return false;
    }

    /**
     * 正序排列，保证优先加密对象
     *
     * @param jsonContent
     * @param jsonPathPatterns
     * @return
     */
    public static Set<String> resolveAllJsonPaths(String jsonContent, Set<String> jsonPathPatterns) {
        DocumentContext pathReadCtx = JsonPath.using(Configuration.builder()
                .options(Option.AS_PATH_LIST).build()).parse(jsonContent);

        SortedSet<String> encryptPaths = Sets.newTreeSet();
        for (String encryptParam : jsonPathPatterns) {
            if (JSON_PATH_ROOT.contains(encryptParam)) {
                return TOTAL_ENCRYPT_PARAMS;
            }
            if (encryptParam.startsWith(JSON_PATH_PREFIX)) {
                List<String> pathList = pathReadCtx.read(encryptParam);
                if (CollectionUtils.isNotEmpty(pathList)) {
                    encryptPaths.addAll(pathList);
                }
            }
        }
        encryptPaths.forEach(LOGGER::debug);
        return encryptPaths;
    }

}
