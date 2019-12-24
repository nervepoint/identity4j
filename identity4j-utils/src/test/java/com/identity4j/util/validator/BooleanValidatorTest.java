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