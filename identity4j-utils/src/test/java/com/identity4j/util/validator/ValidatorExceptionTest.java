/* HEADER */
package com.identity4j.util.validator;

import org.junit.Assert;
import org.junit.Test;

import com.identity4j.util.validator.ValidatorException;

public class ValidatorExceptionTest {

    @Test
    public void messageConstructor() {
        String message = "Error!";
        try {
            throw new ValidatorException(message);
        } catch (ValidatorException mme) {
            Assert.assertEquals(mme.getMessage(), message);
        }
    }

    @Test
    public void messageAndCauseConstructor() {
        String message = "Error!";
        Throwable cause = new IllegalArgumentException();
        try {
            throw new ValidatorException(message, cause);
        } catch (ValidatorException mme) {
            Assert.assertEquals(mme.getMessage(), message);
            Assert.assertEquals(mme.getCause(), cause);
        }
    }
}