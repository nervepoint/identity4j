/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression test: CWE-570 – expression is always false.
 * Before fix: (int) timeStamp == Long.MIN_VALUE was always false because
 * the (int) cast truncated the 64-bit sentinel to 0 before comparison.
 * After fix: the sentinel is detected correctly and 0 is returned.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Verifies that {@code ActiveDirectoryDateUtil.adTimeToJavaDays} correctly
 * handles the {@link Long#MIN_VALUE} sentinel (CWE-570).
 */
public class ActiveDirectoryDateUtilCwe570Test {

    /**
     * Before the fix, the condition {@code (int) Long.MIN_VALUE ==
     * Long.MIN_VALUE} was always false because {@code (int)Long.MIN_VALUE == 0}.
     * After the fix the sentinel value must return 0 days.
     */
    @Test
    public void longMinValueSentinelReturnsZero() {
        int result = ActiveDirectoryDateUtil.adTimeToJavaDays(Long.MIN_VALUE);
        assertEquals("Long.MIN_VALUE sentinel must return 0 (never/no-expiry)", 0, result);
    }

    /**
     * A typical AD interval value (e.g. 7 days expressed in 100-ns units)
     * must not be treated as the sentinel.
     */
    @Test
    public void typicalIntervalIsNotTreatedAsSentinel() {
        // 7 days in 100-nanosecond intervals: 7 * 86400 * 10_000_000
        long sevenDaysAd = 7L * 86400L * 10_000_000L;
        int result = ActiveDirectoryDateUtil.adTimeToJavaDays(sevenDaysAd);
        // The division is: sevenDaysAd / -86400L / 10_000_000L → -7
        assertEquals("7-day AD interval should convert to -7 days", -7, result);
    }

    /**
     * Zero should not match the Long.MIN_VALUE sentinel and must compute
     * to zero days via division (0 / anything == 0).
     */
    @Test
    public void zeroTimestampReturnsZero() {
        int result = ActiveDirectoryDateUtil.adTimeToJavaDays(0L);
        assertEquals(0, result);
    }

    /**
     * Confirms the pre-fix bug: (int)Long.MIN_VALUE is 0, not Long.MIN_VALUE.
     * This test documents why the old code was wrong.
     */
    @Test
    public void intCastOfLongMinValueIsZeroNotSentinel() {
        int narrowed = (int) Long.MIN_VALUE;
        assertNotEquals(
            "Cast of Long.MIN_VALUE to int must NOT equal zero — documents pre-fix bug",
            (long) narrowed, Long.MIN_VALUE);
    }
}
