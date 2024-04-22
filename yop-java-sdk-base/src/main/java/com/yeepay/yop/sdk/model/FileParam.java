/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.model;

import java.io.InputStream;
import java.io.Serializable;

/**
 * title: 文件参数<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2024/4/19
 */
public class FileParam implements Serializable {

    private static final long serialVersionUID = -1L;

    private InputStream fileStream;

    private String fileExtName;

    public FileParam(InputStream fileStream, String fileExtName) {
        this.fileStream = fileStream;
        this.fileExtName = fileExtName;
    }

    public InputStream getFileStream() {
        return fileStream;
    }

    public String getFileExtName() {
        return fileExtName;
    }
}
