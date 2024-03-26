/*
 * Copyright: Copyright (c)2014
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.yop.sdk.invoke.model;

import com.yeepay.yop.sdk.constants.CharacterConstants;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

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

    public static final String RETAIN_RESOURCE_ID = "0000";

    /**
     * 资源分组
     */
    private String resourceGroup = CharacterConstants.EMPTY;

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

    private Callback callback;

    public UriResource(URI uri) {
        this.resource = uri;
        this.resourceType = COMMON;
    }

    public UriResource(String resourceGroup, URI uri) {
        this.resource = uri;
        this.resourceType = COMMON;
        setResourceGroup(resourceGroup);
    }

    public UriResource(ResourceType resourceType, String resourcePrefix, URI uri) {
        this.resourceType = resourceType;
        this.resourcePrefix = resourcePrefix;
        this.resource = uri;
    }

    public UriResource(ResourceType resourceType, String resourcePrefix, URI resource, Callback callback) {
        this.resource = resource;
        this.resourceType = resourceType;
        this.resourcePrefix = resourcePrefix;
        this.callback = callback;
    }

    public UriResource(ResourceType resourceType, String resourceGroup, String resourcePrefix, URI resource) {
        this.resourceType = resourceType;
        setResourceGroup(resourceGroup);
        this.resourcePrefix = resourcePrefix;
        this.resource = resource;
    }

    public URI getResource() {
        return resource;
    }

    public void setResource(URI resource) {
        this.resource = resource;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        if (null != resourceGroup) {
            this.resourceGroup = resourceGroup;
        }
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

    public boolean isRetained() {
        return RETAIN_RESOURCE_ID.equals(this.resourcePrefix);
    }

    public Callback getCallback() {
        return callback;
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
        return this.resourceType + RESOURCE_SEPERATOR + this.resourceGroup + RESOURCE_SEPERATOR
                + this.resourcePrefix + RESOURCE_SEPERATOR + this.resource.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.resourceType, this.resourceGroup, this.resourcePrefix, this.resource);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UriResource) {
            UriResource that = (UriResource) obj;
            return this.resourceType.equals(that.getResourceType())
                    && this.resourceGroup.equals(that.resourceGroup)
                    && this.resourcePrefix.equals(that.resourcePrefix)
                    && this.resource.equals(that.resource);
        }
        return false;
    }

    public String computeResourceKey() {
        return this.resourceType + RESOURCE_SEPERATOR + this.resourceGroup + RESOURCE_SEPERATOR
                + this.resourcePrefix + RESOURCE_SEPERATOR + this.resource.toString();
    }

    public static UriResource parseResourceKey(String resourceKey) {
        final String[] resourceSeperated = resourceKey.split(RESOURCE_SEPERATOR);
        return new UriResource(ResourceType.valueOf(resourceSeperated[0]),
                resourceSeperated[1], resourceSeperated[2], URI.create(resourceSeperated[3]));
    }

    public interface Callback {
        void notify(Object ...args);
    }

}
