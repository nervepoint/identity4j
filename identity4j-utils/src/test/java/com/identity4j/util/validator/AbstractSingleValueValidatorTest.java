/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.AbstractSingleValueValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public abstract class AbstractSingleValueValidatorTest {
    private final String validValue;
    private final String invalidValue;

    protected AbstractSingleValueValidatorTest(String validValue, String invalidValue) {
        this.validValue = validValue;
        this.invalidValue = invalidValue;
    }

    protected final String getValidValue() {
        return validValue;
    }

    protected final String getInvalidValue() {
        return invalidValue;
    }

    @Test(expected = IllegalArgumentException.class)
    public final void nullValues() {
        Validator validator = createValidator();
        validator.validate(null, (String[]) null);
    }

    @Test
    public final void invalid() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, invalidValue);
        assertEquals(1, validate.size());
    }

    @Test
    public final void multipleInvalid() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, invalidValue, invalidValue);
        assertEquals(2, validate.size());
    }

    @Test
    public final void noValues() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null);
        assertEquals(validator.isValueRequired()? 1 : 0, validate.size());
    }

    @Test
    public final void empty() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, "");
        assertEquals(validator.isValueRequired()? 1 : 0, validate.size());
    }

    @Test
    public final void valid() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, validValue);
        assertTrue(validate.isEmpty());
    }

    @Test
    public final void mulipleValid() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, validValue, validValue);
        assertTrue(validate.isEmpty());
    }

    @Test
    public final void validTrimmedByDefault() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, " " + validValue);
        assertTrue(validate.isEmpty());
    }

    @Test
    public final void validTrimSpecified() {
        Validator validator = createValidator(Collections.singletonMap(AbstractSingleValueValidator.TRIM, new String[] { "true" }));
        Collection<ValidationError> validate = validator.validate(null, " " + validValue);
        assertTrue(validate.isEmpty());
    }

    @Test
    public void validTrimDisabled() {
        Validator validator = createValidator(Collections.singletonMap(AbstractSingleValueValidator.TRIM, new String[] { "false" }));
        Collection<ValidationError> validate = validator.validate(null, " " + validValue);
        assertEquals(1, validate.size());
    }

    protected Map<String, String[]> createValidatorParameterMap() {
        return new HashMap<String, String[]>();
    }

    protected final Validator createValidator() {
        return createValidator(createValidatorParameterMap());
    }

    protected final Validator createValidator(Map<String, String[]> parameters) {
        return createValidator(new MultiMap(parameters));
    }

    protected abstract Validator createValidator(MultiMap multiMap);
}