package com.yeepay.yop.sdk;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Specifies constants that define YOP Regions.
 */
public enum Region {

    CN_N1(""),  // CDN 入口
    CN_SJ("sj"),// 世纪互联
    CN_DJ("dj"),// 电信
    CN_M6("m6");    // 测试

    /**
     * The list of ID's representing each region.
     */
    private final List<String> regionIds;

    /**
     * Constructs a new region with the specified region ID's.
     *
     * @param regionIds The list of ID's representing the YOP region.
     * @throws NullPointerException     regionIds should not be null.
     * @throws IllegalArgumentException regionIds should not be empty.
     */
    Region(String... regionIds) {
        checkNotNull(regionIds, "regionIds should not be null.");
        checkArgument(regionIds.length > 0, "regionIds should not be empty");
        this.regionIds = Arrays.asList(regionIds);
    }

    @Override
    public String toString() {
        return this.regionIds.get(0);
    }

    /**
     * Returns the YOP Region enumeration value representing the specified YOP Region ID string.
     * If specified string doesn't map to a known YOP Region, then an <code>IllegalArgumentException</code> is thrown.
     *
     * @param regionId The YOP region ID string.
     * @return The YOP Region enumeration value representing the specified YOP Region ID.
     * @throws NullPointerException regionId should not be null.
     */
    public static Region fromValue(String regionId) {
        checkNotNull(regionId, "regionId should not be null.");
        for (Region region : Region.values()) {
            List<String> regionIds = region.regionIds;
            if (regionIds != null && regionIds.contains(regionId)) {
                return region;
            }
        }
        throw new IllegalArgumentException("Cannot create region from " + regionId);
    }
}
