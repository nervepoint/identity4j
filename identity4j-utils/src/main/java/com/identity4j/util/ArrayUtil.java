/* HEADER */
package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


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
