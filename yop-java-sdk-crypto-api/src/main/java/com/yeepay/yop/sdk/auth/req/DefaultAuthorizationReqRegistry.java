package com.yeepay.yop.sdk.auth.req;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * title: AuthorizationReq注册中心<br>
 * description: <br>
 * Copyright: Copyright (c) 2017<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 17/12/1 17:50
 */
public class DefaultAuthorizationReqRegistry implements AuthorizationReqRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthorizationReqRegistry.class);

    private final Map<String, List<AuthorizationReq>> authorizationReqs;

    public DefaultAuthorizationReqRegistry() {
        authorizationReqs = Maps.newHashMap();
    }

    @Override
    public void register(String operationId, String securityReqs) {
        String[] splitReqs = securityReqs.split(CharacterConstants.COMMA);
        List<AuthorizationReq> authorizationReqList = Lists.newArrayListWithExpectedSize(splitReqs.length);
        for (String securityReq : splitReqs) {
            AuthorizationReq authorizationReq = AuthorizationReqSupport.getAuthorizationReq(securityReq);
            if (null == authorizationReq) {
                LOGGER.warn("unsupported security req:{}", securityReq);
            } else {
                authorizationReqList.add(authorizationReq);
            }
        }
        authorizationReqs.put(operationId, authorizationReqList);
    }

    @Override
    public List<AuthorizationReq> getAuthorizationReq(String operationId) {
        return authorizationReqs.get(operationId);
    }
}
