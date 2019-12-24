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