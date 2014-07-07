/* HEADER */
package com.identity4j.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides general purpose Array utilities.
 */
public class ArrayUtil {

    private ArrayUtil() {
        // don't create an instance
    }

    /**
     * Copy a string array.
     * 
     * @param originalArray
     * @return new string array
     */
    public static String[] copyArray(String[] originalArray) {
        String[] copyOf = new String[originalArray.length];
        System.arraycopy(originalArray, 0, copyOf, 0, originalArray.length);
        return copyOf;
    }

    public static Object[] reverseArray(Object[] array) {
        List<Object> o = new ArrayList<Object>(Arrays.asList(array));
        Collections.reverse(o);
        return o.toArray();
    }
}
