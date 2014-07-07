/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.Collections;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value is a valid
 * {@link java.util.Date}.
 */
public class DateValidator extends AbstractSingleValueValidator {

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public DateValidator(MultiMap parameters) {
        super(parameters);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        try {
            Long.valueOf(value);
        } catch (NumberFormatException nfe) {
            return Collections.singleton(new ValidationError("date.value.invalid", context, value));
        }
        return Collections.emptyList();
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        return builder.toString();
    }
}