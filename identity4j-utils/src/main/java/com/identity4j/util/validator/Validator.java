/* HEADER */
package com.identity4j.util.validator;

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