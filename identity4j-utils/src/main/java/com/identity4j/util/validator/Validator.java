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

/**
 * A validator for application-specific objects.
 * <p>
 * This interface is totally divorced from any infrastructure or context; that
 * is to say it is not coupled to validating only objects in the web tier, the
 * data-access tier, or the whatever-tier. As such it is amenable to being used
 * in any layer of an application, and supports the encapsulation of validation
 * logic as first-class citizens in their own right.
 */
public interface Validator {

	/**
	 * Validate the supplied values, each value is validated individually.
	 * @param context TODO
	 * @param values the values to validate
	 * 
	 * @return the collection of validation failures
	 */
	Collection<ValidationError> validate(ValidationContext context, String... values);

	/**
	 * Get if the validator's rules would mean the a field value is required.
	 * This is a general hint.
	 * 
	 * @return required
	 */
	boolean isValueRequired();
}