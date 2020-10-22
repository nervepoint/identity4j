/* HEADER */
package com.identity4j.util.validator;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;

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