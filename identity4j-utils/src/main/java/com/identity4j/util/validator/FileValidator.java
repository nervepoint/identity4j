/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.Collections;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value is a valid
 * {@link java.io.File}.
 */
public class FileValidator extends AbstractSingleValueValidator {

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public FileValidator(MultiMap parameters) {
        super(parameters);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        // TODO what can we validate here
        return Collections.emptyList();
    }
}