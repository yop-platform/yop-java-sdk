package com.yeepay.g3.sdk.yop.error;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class YopError {

    private String code;

    private String subCode;

    private String message;

    private String subMessage;

    private String docUrl;

    private String solution;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubMessage() {
        return subMessage;
    }

    public void setSubMessage(String subMessage) {
        this.subMessage = subMessage;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static final class Builder {
        private String code;
        private String subCode;
        private String message;
        private String subMessage;
        private String docUrl;
        private String solution;

        private Builder() {
        }

        public static Builder anYopError() {
            return new Builder();
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withSubCode(String subCode) {
            this.subCode = subCode;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withSubMessage(String subMessage) {
            this.subMessage = subMessage;
            return this;
        }

        public Builder withDocUrl(String docUrl) {
            this.docUrl = docUrl;
            return this;
        }

        public Builder withSolution(String solution) {
            this.solution = solution;
            return this;
        }

        public YopError build() {
            YopError yopError = new YopError();
            yopError.setCode(code);
            yopError.setSubCode(subCode);
            yopError.setMessage(message);
            yopError.setSubMessage(subMessage);
            yopError.setDocUrl(docUrl);
            yopError.setSolution(solution);
            return yopError;
        }
    }
}
