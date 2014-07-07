/* HEADER */
package com.identity4j.util.validator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value conforms to the
 * rules set out by the {@link java.net.URL} implementation.
 */
public class UrlValidator extends AbstractSingleValueValidator {

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public UrlValidator(MultiMap parameters) {
        super(parameters);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        try {
            new URL(value);
        } catch (MalformedURLException mue) {
            return Collections.singleton(new ValidationError("url.value.invalid", context, value));
        }
        return Collections.emptyList();
    }
}