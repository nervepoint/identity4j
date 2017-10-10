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
 * A validator implementation that checks the supplied value conforms to either
 * the {@link #TRUE_VALUE} or the {@link #FALSE_VALUE}. If the
 * {@link #TRUE_VALUE} or {@link #FALSE_VALUE} is not specified, the validator
 * uses the default values.
 */
public class BooleanValidator extends AbstractSingleValueValidator {
	/**
	 * Parameter which when supplied, specifies the true value valid for the
	 * string. If this value is not supplied, the validator defaults to a true
	 * value of true.
	 */
	public static final String TRUE_VALUE = "TRUE_VALUE";
	/**
	 * Parameter which when supplied, specifies the false value valid for the
	 * string. If this value is not supplied, the validator defaults to a false
	 * value of false.
	 */
	public static final String FALSE_VALUE = "FALSE_VALUE";
	private final String trueValue;
	private final String falseValue;

	/**
	 * Constructor.
	 * 
	 * @param parameters parameters
	 */
	public BooleanValidator(MultiMap parameters) {
		super(parameters);
		trueValue = parameters.getStringOrDefault(TRUE_VALUE, "true");
		falseValue = parameters.getStringOrDefault(FALSE_VALUE, "false");
	}

	@Override
	final Collection<ValidationError> validate(ValidationContext context, String value) {
		if (!trueValue.equals(value) && !falseValue.equals(value)) {
			return Collections.singleton(new ValidationError("boolean.value.invalid", context, value, trueValue, falseValue));
		}
		return Collections.emptyList();
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[trueValue='").append(trueValue);
		builder.append("', falseValue='").append(falseValue).append("']");
		return builder.toString();
	}

	@Override
	public boolean isValueRequired() {
		return false;
	}
}