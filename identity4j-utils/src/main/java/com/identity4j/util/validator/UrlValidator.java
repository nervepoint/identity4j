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