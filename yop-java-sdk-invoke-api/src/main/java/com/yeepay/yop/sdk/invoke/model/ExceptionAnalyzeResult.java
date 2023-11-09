/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

import java.io.Serializable;

/**
 * title: 异常分析结果<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/7
 */
public class ExceptionAnalyzeResult implements Serializable {

    private static final long serialVersionUID = -1L;

    private Throwable exception;
    private boolean needRetry;
    private boolean needDegrade;

    private String exDetail;

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public boolean isNeedRetry() {
        return needRetry;
    }

    public void setNeedRetry(boolean needRetry) {
        this.needRetry = needRetry;
    }

    public boolean isNeedDegrade() {
        return needDegrade;
    }

    public void setNeedDegrade(boolean needDegrade) {
        this.needDegrade = needDegrade;
    }

    public void setExDetail(String exDetail) {
        this.exDetail = exDetail;
    }

    public String getExDetail() {
        return exDetail;
    }

}
