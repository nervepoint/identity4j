/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.AbstractSingleValueValidator;
import com.identity4j.util.validator.RegexValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public class RegexValidatorTest extends AbstractSingleValueValidatorTest {

    public RegexValidatorTest() {
        super("aaaaab", "!�$%^&*()");
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
        Map<String, String[]> parameters = super.createValidatorParameterMap();
        parameters.put(RegexValidator.REGEX, new String[] { "a*b" });
        return parameters;
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new RegexValidator(multiMap);
    }
}