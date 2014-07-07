/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.IntegerValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public class IntegerValidatorTest extends AbstractSingleValueValidatorTest {

    public IntegerValidatorTest() {
        super("5", "unknown");
    }

    @Test
    public final void defaultParameters() {
        Validator validator = createValidator(Collections.<String, String[]> emptyMap());
        Collection<ValidationError> invalid = validator.validate(null, getInvalidValue());
        assertEquals(1, invalid.size());
        Collection<ValidationError> valid = validator.validate(null, getValidValue());
        assertTrue(valid.isEmpty());
    }

    @Test
    public final void lessThanMinimum() {
        Validator validator = createValidator();
        Collection<ValidationError> invalid = validator.validate(null, "0");
        assertEquals(1, invalid.size());
    }

    @Test
    public final void greaterThanMaximum() {
        Validator validator = createValidator();
        Collection<ValidationError> invalid = validator.validate(null, "11");
        assertEquals(1, invalid.size());
    }

    @Override
    protected Map<String, String[]> createValidatorParameterMap() {
        Map<String, String[]> parameters = super.createValidatorParameterMap();
        parameters.put(IntegerValidator.MINIMUM_VALUE, new String[] { "1" });
        parameters.put(IntegerValidator.MAXIMUM_VALUE, new String[] { "10" });
        return parameters;
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new IntegerValidator(multiMap);
    }
}