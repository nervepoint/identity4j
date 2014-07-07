/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.Collections;

import com.identity4j.util.Color;
import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value is an RGB color.
 */
public class ColorValidator extends AbstractSingleValueValidator {

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public ColorValidator(MultiMap parameters) {
        super(parameters);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        try {
            new Color(value);
        } catch (IllegalArgumentException iae) {
            return Collections.singleton(new ValidationError("color.value.invalid", context, value));
        }
        return Collections.emptyList();
    }
}