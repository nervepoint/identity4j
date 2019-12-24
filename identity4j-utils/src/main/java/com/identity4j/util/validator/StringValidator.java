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
 * A validator implementation that checks the supplied value conforms to the
 * supplied {@link #MINIMUM_LENGTH} and {@link #MAXIMUM_LENGTH}. If the
 * {@link #MINIMUM_LENGTH} or {@link #MAXIMUM_LENGTH} is not specified, the
 * validator uses the default values.
 */
public class StringValidator extends AbstractSingleValueValidator {
    /**
     * Parameter which when supplied, specifies the minimum allowed length for
     * the string. If this value is not supplied, the validator defaults to a
     * minimum length of 0.
     */
    public static final String MINIMUM_LENGTH = "MINIMUM_LENGTH";
    /**
     * Parameter which when supplied, specifies the maximum allowed length for
     * the string. If this value is not supplied, the validator defaults to a
     * maximum length of 4096 * 16.
     */
    public static final String MAXIMUM_LENGTH = "MAXIMUM_LENGTH";

    private final int minimumLength;
    private final int maximumLength;

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public StringValidator(MultiMap parameters) {
        super(parameters);
        minimumLength = parameters.getIntegerOrDefault(MINIMUM_LENGTH, 0);
        maximumLength = parameters.getIntegerOrDefault(MAXIMUM_LENGTH, 4096 * 16);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        if (value.length() < minimumLength) {
            return Collections.singleton(new ValidationError("string.length.too.small", context, value, minimumLength));
        } else if (value.length() > maximumLength) {
            return Collections.singleton(new ValidationError("string.length.too.large", context, value, maximumLength));
        }
        return Collections.emptyList();
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("[minimumLength='").append(minimumLength);
        builder.append("', maximumLength='").append(maximumLength).append("']");
        return builder.toString();
    }

	@Override
	public boolean isValueRequired() {
		return super.isValueRequired() || minimumLength > 0;
	}
}