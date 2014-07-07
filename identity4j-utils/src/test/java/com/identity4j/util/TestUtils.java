/* HEADER */
package com.identity4j.util;

import org.junit.Ignore;

@Ignore
public class TestUtils {

    public void whatever() {
        // Maven thinks this is a test class
    }

    public static final String randomValue() {
        double currentTimeMillis = System.currentTimeMillis() * Math.random();
        return String.valueOf(Math.round(currentTimeMillis));
    }

    public static String generateStringToLength(int length) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < length; index++) {
            builder.append("0");
        }
        return builder.toString();
    }
}