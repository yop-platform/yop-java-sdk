/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.config.provider.file;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

import static com.yeepay.yop.sdk.YopConstants.DEFAULT_YOP_CERT_STORE_PATH;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2/5/21 2:31 PM
 */
public class YopCertStore implements Serializable {

    private static final long serialVersionUID = -1L;
    private static final String DEFAULT_PATH = "/tmp/yop/certs";

    public YopCertStore() {
    }

    public YopCertStore(String path) {
        this.path = path;
    }

    /**
     * 是否开启本地存储
     */
    private Boolean enable = true;

    /**
     * 是否懒加载，默认启动时加载
     */
    private Boolean lazy;

    /**
     * 本地存储路径：绝对路径
     */
    private String path = DEFAULT_PATH;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean getLazy() {
        return lazy;
    }

    public void setLazy(Boolean lazy) {
        this.lazy = lazy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = StringUtils.defaultString(path, DEFAULT_YOP_CERT_STORE_PATH);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
