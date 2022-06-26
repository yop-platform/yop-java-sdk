/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.auth.signer;

import com.google.common.collect.Lists;
import com.yeepay.yop.sdk.security.SignerTypeEnum;
import com.yeepay.yop.sdk.utils.SmInitUtils;

import java.util.List;

/**
 * title: YopSm2Signer<br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author yunmei.wu
 * @version 1.0.0
 * @since 2021/1/18 3:25 下午
 */
public class YopSm2Signer extends AbstractYopPKISigner {

    static {
        SmInitUtils.init();
    }

    @Override
    public List<String> supportSignerAlg() {
        return Lists.newArrayList(SignerTypeEnum.SM2.name());
    }
}
