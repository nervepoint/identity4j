/* HEADER */
package com.identity4j.util.validator;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.DateValidator;
import com.identity4j.util.validator.Validator;

public class DateValidatorTest extends AbstractSingleValueValidatorTest {

    public DateValidatorTest() {
        super(String.valueOf(System.currentTimeMillis()), "unknown");
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new DateValidator(multiMap);
    }
}