/* HEADER */
package com.identity4j.util;

import org.junit.Test;

import com.identity4j.util.Util;

public class UtilTest {

    @Test(expected = IllegalArgumentException.class)
    public void assertNotNullWithNullValue() {
        Util.assertNotNull(null, "");
    }

    @Test
    public void assertNotNull() {
        Util.assertNotNull("", "");
    }
}