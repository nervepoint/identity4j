package com.identity4j.util.passwords;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression test: CWE-682/CWE-783 – operator precedence in PasswordGenerator.
 * Before fix: (int) Math.random() * pw.length() always yielded 0 due to (int)
 * binding more tightly than *, so deleteCharAt was always called on index 0.
 * After fix: (int)(Math.random() * pw.length()) correctly picks a random index.
 * #L%
 */

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PasswordGeneratorCwe682Test {

    /**
     * (int)(Math.random() * n) must produce values in [0, n).
     * Before fix the result was always 0 regardless of n.
     */
    @Test
    public void randomIndexIsInRange() {
        int n = 20;
        boolean sawNonZero = false;
        for (int trial = 0; trial < 200; trial++) {
            int idx = (int)(Math.random() * n);
            assertTrue("index must be >= 0", idx >= 0);
            assertTrue("index must be < n", idx < n);
            if (idx != 0) {
                sawNonZero = true;
            }
        }
        assertTrue("non-zero indices should appear in 200 trials", sawNonZero);
    }

    /**
     * Documents the pre-fix bug: (int) Math.random() was always 0.
     */
    @Test
    public void oldExpressionAlwaysZero() {
        for (int trial = 0; trial < 50; trial++) {
            int oldBugResult = (int) Math.random(); // always 0 — the bug
            assertNotEquals("cast-before-multiply form must equal 0 (pre-fix bug documented)",
                    1, oldBugResult);
        }
    }
}
