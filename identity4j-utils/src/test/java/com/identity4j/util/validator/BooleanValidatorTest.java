/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.BooleanValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public class BooleanValidatorTest extends AbstractSingleValueValidatorTest {

    public BooleanValidatorTest() {
        super("true", "unknown");
    }

    @Test
    public final void defaultParameters() {
        Validator validator = createValidator(Collections.<String, String[]> emptyMap());
        Collection<ValidationError> invalid = validator.validate(null, getInvalidValue());
        assertEquals(1, invalid.size());
        Collection<ValidationError> valid = validator.validate(null, getValidValue());
        assertTrue(valid.isEmpty());
    }

    @Override
    protected Map<String, String[]> createValidatorParameterMap() {
        Map<String, String[]> parameters = super.createValidatorParameterMap();
        parameters.put(BooleanValidator.TRUE_VALUE, new String[] { "true" });
        parameters.put(BooleanValidator.FALSE_VALUE, new String[] { "false" });
        return parameters;
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new BooleanValidator(multiMap);
    }
}