/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

import com.yeepay.yop.sdk.constants.CharacterConstants;

import java.io.Serializable;
import java.net.URI;

import static com.yeepay.yop.sdk.invoke.model.UriResource.ResourceType.COMMON;

/**
 * title: URI资源包装类<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wdc
 * @version 1.0.0
 * @since 2023/11/28
 */
public class UriResource implements Serializable {

    private static final long serialVersionUID = -1L;

    public static final String RESOURCE_SEPERATOR = "####";

    /**
     * URI路径
     */
    private URI resource;

    /**
     * URI资源标识
     */
    private ResourceType resourceType;

    /**
     * URI资源前缀
     */
    private String resourcePrefix = CharacterConstants.EMPTY;

    public UriResource(URI uri) {
        this.resource = uri;
        this.resourceType = COMMON;
    }

    public UriResource(ResourceType resourceType, String resourcePrefix, URI uri) {
        this.resource = uri;
        this.resourceType = resourceType;
        this.resourcePrefix = resourcePrefix;
    }

    public URI getResource() {
        return resource;
    }

    public void setResource(URI resource) {
        this.resource = resource;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourcePrefix() {
        return resourcePrefix;
    }

    public void setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    public enum ResourceType {
        /**
         * 正常资源
         */
        COMMON,

        /**
         * 熔断资源
         */
        BLOCKED
    }

    @Override
    public String toString() {
        return this.resourceType + RESOURCE_SEPERATOR + this.resourcePrefix + RESOURCE_SEPERATOR + this.resource.toString();
    }

    public String computeResourceKey() {
        if (COMMON.equals(this.resourceType)) {
            return this.resource.toString();
        }
        return this.toString();
    }

    public static UriResource parseResourceKey(String resourceKey) {
        final String[] resourceSeperated = resourceKey.split(RESOURCE_SEPERATOR);
        if (resourceSeperated.length == 1) {
            return new UriResource(URI.create(resourceKey));
        }
        return new UriResource(ResourceType.valueOf(resourceSeperated[0]),
                resourceSeperated[1], URI.create(resourceSeperated[2]));
    }
}
