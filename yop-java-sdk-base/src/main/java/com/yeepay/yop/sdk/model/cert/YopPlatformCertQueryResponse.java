/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.model.cert;

import com.yeepay.yop.sdk.model.BaseResponse;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * title: YOP平台证书查询接口返回<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2022/4/26
 */
public class YopPlatformCertQueryResponse extends BaseResponse {
    private static final long serialVersionUID = 1L;

    private YopPlatformCertQueryResult result;

    public YopPlatformCertQueryResult getResult() {
        return result;
    }

    public void setResult(YopPlatformCertQueryResult result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
