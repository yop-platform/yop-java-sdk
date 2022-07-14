/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.gm.security.cert.parser;

import com.yeepay.yop.sdk.config.provider.file.YopCertConfig;
import com.yeepay.yop.sdk.security.cert.YopCertCategory;
import com.yeepay.yop.sdk.base.security.cert.parser.YopCertParser;
import com.yeepay.yop.sdk.security.CertTypeEnum;
import com.yeepay.yop.sdk.constants.CharacterConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * title: Sm4密钥解析器<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/5/4
 */
public class YopSm4CertParser implements YopCertParser {

    @Override
    public String parse(YopCertConfig certConfig) {
        return certConfig.getValue();
    }

    @Override
    public String parserId() {
        return StringUtils.joinWith(CharacterConstants.COMMA, YopCertCategory.SECRET, CertTypeEnum.SM4);
    }
}
