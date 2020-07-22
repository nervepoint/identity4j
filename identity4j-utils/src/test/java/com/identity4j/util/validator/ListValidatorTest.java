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

public class ListValidatorTest {
    private final Validator validator;

    public ListValidatorTest() {
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put(ListValidator.LIST_VALUES, new String[] { "one!two!three" });
        parameters.put(ListValidator.MINIMUM_SELECTION, new String[] { "1" });
        parameters.put(ListValidator.MAXIMUM_SELECTION, new String[] { "2" });
        validator = createValidator(parameters);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void nullValues() {
        validator.validate(null, (String[]) null);
    }

    @Test
    public final void invalid() {
        Collection<ValidationError> validate = validator.validate(null, "four");
        assertEquals(1, validate.size());
    }

    @Test
    public final void multipleInvalid() {
        Collection<ValidationError> validate = validator.validate(null, "four", "four");
        assertEquals(2, validate.size());
    }

    @Test
    public final void valid() {
        Collection<ValidationError> validate = validator.validate(null, "one");
        assertTrue(validate.isEmpty());
    }

    @Test
    public final void mulipleValid() {
        Collection<ValidationError> validate = validator.validate(null, "one", "two");
        assertTrue(validate.isEmpty());
    }

    @Test
    public final void validTrimmedByDefault() {
        Collection<ValidationError> validate = validator.validate(null, " one ");
        assertTrue(validate.isEmpty());
    }

    @Test
    public final void validTrimSpecified() {
        Validator validator = createValidator(Collections.singletonMap(ListValidator.TRIM, new String[] { "true" }));
        Collection<ValidationError> validate = validator.validate(null, " one ");
        assertTrue(validate.isEmpty());
    }

    @Test
    public void validTrimDisabled() {
        Map<String, String[]> parameters = new HashMap<String, String[]>();
        parameters.put(ListValidator.TRIM, new String[] { "false" });
        parameters.put(ListValidator.LIST_VALUES, new String[] { "one!two!three" });
        parameters.put(ListValidator.MINIMUM_SELECTION, new String[] { "1" });
        parameters.put(ListValidator.MAXIMUM_SELECTION, new String[] { "2" });

        Validator validator = createValidator(parameters);
        Collection<ValidationError> validate = validator.validate(null, " one ");
        assertEquals(1, validate.size());
    }

    @Test
    public final void minimumSelection() {
        Collection<ValidationError> validate = validator.validate(null);
        assertEquals(1, validate.size());
    }

    @Test
    public final void maximumSelection() {
        Collection<ValidationError> validate = validator.validate(null, "one", "two", "three");
        assertEquals(1, validate.size());
    }

    private Validator createValidator(Map<String, String[]> parameters) {
        return new ListValidator(new MultiMap(parameters));
    }
}