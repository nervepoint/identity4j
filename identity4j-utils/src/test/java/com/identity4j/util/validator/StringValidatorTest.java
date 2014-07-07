/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.TestUtils;
import com.identity4j.util.validator.AbstractSingleValueValidator;
import com.identity4j.util.validator.StringValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public class StringValidatorTest extends AbstractSingleValueValidatorTest {

    public StringValidatorTest() {
        super("value", "");
    }

    @Test
    public final void defaultParameters() {
        Validator validator = createValidator(Collections.<String, String[]> emptyMap());
        assertTrue(validator.validate(null, getInvalidValue()).isEmpty());
        assertTrue(validator.validate(null, getValidValue()).isEmpty());
    }

    @Test
    public final void minimumLength() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, "");
        assertEquals(1, validate.size());
    }

    @Test
    public final void maximumLength() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, TestUtils.generateStringToLength(11));
        assertEquals(1, validate.size());
    }

    @Override
    @Test
    public void validTrimDisabled() {
        Validator validator = createValidator(Collections.singletonMap(AbstractSingleValueValidator.TRIM, new String[] { "false" }));
        Collection<ValidationError> validate = validator.validate(null, " " + getValidValue());
        assertTrue(validate.isEmpty());
    }

    @Override
    protected Map<String, String[]> createValidatorParameterMap() {
        Map<String, String[]> parameterMap = super.createValidatorParameterMap();
        parameterMap.put(StringValidator.MINIMUM_LENGTH, new String[] { "1" });
        parameterMap.put(StringValidator.MAXIMUM_LENGTH, new String[] { "10" });
        return parameterMap;
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new StringValidator(multiMap);
    }
}