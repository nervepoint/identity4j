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
import com.identity4j.util.validator.ListValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

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