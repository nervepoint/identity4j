/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.AbstractSingleValueValidator;
import com.identity4j.util.validator.UrlValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public class UrlValidatorTest extends AbstractSingleValueValidatorTest {

    public UrlValidatorTest() {
        super("http://www.helloworld.com", "www.helloworld.com");
    }

    @Override
    @Test
    public void validTrimDisabled() {
        Validator validator = createValidator(Collections.singletonMap(AbstractSingleValueValidator.TRIM, new String[] { "false" }));
        Collection<ValidationError> validate = validator.validate(null, " " + getValidValue());
        assertTrue(validate.isEmpty());
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new UrlValidator(multiMap);
    }
}