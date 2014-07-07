/* HEADER */
package com.identity4j.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides general purpose Map utilities.
 */
public final class MapUtil {

    private MapUtil() {
        // don't create an instance
    }

    /**
     * Convert a string array map to a string map.
     * 
     * @param toTransform
     * @return string map
     */
    public static Map<String, String> toStringMap(Map<String, String[]> toTransform) {
        Map<String, String> values = new HashMap<String, String>();
        for (Entry<String, String[]> entry : toTransform.entrySet()) {
            values.put(entry.getKey(), StringUtil.toDefaultString(entry.getValue()));
        }
        return values;
    }

    /**
     * Convert a string map to a string array map.
     *  
     * @param toTransform
     * @return string array map
     */
    public static Map<String, String[]> toStringArrayMap(Map<String, String> toTransform) {
        Map<String, String[]> values = new HashMap<String, String[]>();
        for (Entry<String, String> entry : toTransform.entrySet()) {
            values.put(entry.getKey(), StringUtil.toDefaultArray(entry.getValue()));
        }
        return values;
    }

    /**
     * Create a copy of a string array map. The map itself and all of its
     * entries will be copied.
     * 
     * @param original original string array map
     * @return copy of string array map
     */
    public static Map<String, String[]> copyMap(Map<String, String[]> original) {
        Map<String, String[]> copyOf = new HashMap<String, String[]>();
        for (Entry<String, String[]> entry : original.entrySet()) {
            copyOf.put(entry.getKey(), ArrayUtil.copyArray(entry.getValue()));
        }
        return copyOf;
    }
}