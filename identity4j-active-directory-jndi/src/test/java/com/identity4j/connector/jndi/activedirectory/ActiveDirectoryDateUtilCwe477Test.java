/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression test: CWE-477 – deprecated javaDataToADTime(Date) removed;
 * javaDateToADTime(Date) must remain correct.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

/**
 * Verifies ActiveDirectoryDateUtil.javaDateToADTime behavior after the
 * misspelled deprecated alias javaDataToADTime was removed (CWE-477).
 */
public class ActiveDirectoryDateUtilCwe477Test {

    /** AD epoch: 1 January 1601 UTC in milliseconds since Unix epoch. */
    private static final long AD_EPOCH_MILLIS = -11644473600000L;

    /** 100-nanosecond intervals per millisecond. */
    private static final long HUNDRED_NS_PER_MS = 10000L;

    @Test
    public void javaDateToADTimeReturnsCorrectFiletimeForKnownDate() {
        // 1 January 2000 00:00:00 UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date date = cal.getTime();

        long adTime = ActiveDirectoryDateUtil.javaDateToADTime(date);

        // Expected: ms difference from AD epoch × 10000
        long expectedMs = date.getTime() - AD_EPOCH_MILLIS;
        long expected = expectedMs * HUNDRED_NS_PER_MS;
        assertEquals(expected, adTime);
    }

    @Test
    public void adTimeToJavaDateRoundTripIsConsistent() {
        // Convert a Date to AD time and back; should return the same millisecond.
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2024, Calendar.AUGUST, 1, 12, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date original = cal.getTime();

        long adTime = ActiveDirectoryDateUtil.javaDateToADTime(original);
        Date recovered = ActiveDirectoryDateUtil.adTimeToJavaDate(adTime);

        assertEquals(original.getTime(), recovered.getTime());
    }

    @Test
    public void javaDateToADTimeIsPositiveForDatesAfter1601() {
        Date now = new Date();
        long adTime = ActiveDirectoryDateUtil.javaDateToADTime(now);
        assertTrue("AD time must be positive for dates after 1601", adTime > 0);
    }
}
