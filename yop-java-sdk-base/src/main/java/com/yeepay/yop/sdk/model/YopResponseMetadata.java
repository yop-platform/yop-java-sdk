package com.yeepay.yop.sdk.model;

import java.util.Date;

/**
 * Represents additional metadata included with a response from YOP.
 */
public class YopResponseMetadata {

    private String yopRequestId;

    private String yopSign;

    private String yopContentSha256;

    private String yopVia;

    private String contentDisposition;

    private String transferEncoding;

    private String contentEncoding;

    private long contentLength = -1;

    private String contentMd5;

    private String contentRange;

    private String contentType;

    private Date date;

    private String eTag;

    private Date expires;

    private Date lastModified;

    private String server;

    private String location;

    private String yopCertSerialNo;

    private String yopEncrypt;

    public String getYopRequestId() {
        return this.yopRequestId;
    }

    public void setYopRequestId(String yopRequestId) {
        this.yopRequestId = yopRequestId;
    }

    public String getYopSign() {
        return yopSign;
    }

    public void setYopSign(String yopSign) {
        this.yopSign = yopSign;
    }

    public String getYopContentSha256() {
        return this.yopContentSha256;
    }

    public void setYopContentSha256(String yopContentSha256) {
        this.yopContentSha256 = yopContentSha256;
    }

    public String getYopVia() {
        return yopVia;
    }

    public void setYopVia(String yopVia) {
        this.yopVia = yopVia;
    }

    public String getContentDisposition() {
        return this.contentDisposition;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public long getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentMd5() {
        return this.contentMd5;
    }

    public void setContentMd5(String contentMd5) {
        this.contentMd5 = contentMd5;
    }

    public String getContentRange() {
        return this.contentRange;
    }

    public void setContentRange(String contentRange) {
        this.contentRange = contentRange;
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getETag() {
        return this.eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public Date getExpires() {
        return this.expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public void setTransferEncoding(String transferEncoding) {
        this.transferEncoding = transferEncoding;
    }

    public String getYopCertSerialNo() {
        return yopCertSerialNo;
    }

    public void setYopCertSerialNo(String yopCertSerialNo) {
        this.yopCertSerialNo = yopCertSerialNo;
    }

    public String getYopEncrypt() {
        return yopEncrypt;
    }

    public void setYopEncrypt(String yopEncrypt) {
        this.yopEncrypt = yopEncrypt;
    }

    @Override
    public String toString() {
        return "YopResponseMetadata [\n  yopRequestId=" + yopRequestId
                + ", \n  yopContentSha256=" + yopContentSha256
                + ", \n  yopSign=" + yopSign
                + ", \n  yopVia=" + yopVia
                + ", \n  contentDisposition=" + contentDisposition
                + ", \n  contentEncoding=" + contentEncoding + ", \n  contentLength="
                + contentLength + ", \n  contentMd5=" + contentMd5
                + ", \n  contentRange=" + contentRange + ", \n  contentType="
                + contentType + ", \n  date=" + date + ", \n  eTag=" + eTag
                + ", \n  expires=" + expires + ", \n  lastModified=" + lastModified
                + ", \n  server=" + server + ", \n  location=" + location
                + ", \n  yopCertSerialNo=" + yopCertSerialNo
                + ", \n  yopEncrypt=" + yopEncrypt
                + "]";
    }

}
