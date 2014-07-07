/* HEADER */
package com.identity4j.util;

import org.junit.Assert;
import org.junit.Test;

import com.identity4j.util.MultiMapException;

public class MultiMapExceptionTest {

    @Test
    public void messageConstructor() {
        String message = "Error!";
        try {
            throw new MultiMapException(message);
        } catch (MultiMapException mme) {
            Assert.assertEquals(mme.getMessage(), message);
        }
    }

    @Test
    public void messageAndCauseConstructor() {
        String message = "Error!";
        Throwable cause = new IllegalArgumentException();
        try {
            throw new MultiMapException(message, cause);
        } catch (MultiMapException mme) {
            Assert.assertEquals(mme.getMessage(), message);
            Assert.assertEquals(mme.getCause(), cause);
        }
    }
}