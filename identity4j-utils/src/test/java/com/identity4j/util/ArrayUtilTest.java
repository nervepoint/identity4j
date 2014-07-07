package com.identity4j.util;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.identity4j.util.ArrayUtil;

public class ArrayUtilTest {

    @Test
    public void copyArray() {
        String[] arr = { "value1", "value2", "value3" };
        assertArrayEquals(ArrayUtil.copyArray(arr), arr);
    }

}
