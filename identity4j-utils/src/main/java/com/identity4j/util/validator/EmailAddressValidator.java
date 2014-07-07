/* HEADER */
package com.identity4j.util.validator;

import java.util.regex.Pattern;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value is a valid email
 * address or host name.
 */
public class EmailAddressValidator extends RegexValidator {

	/**
	 * Constructor.
	 * 
	 * @param parameters parameters
	 */
	public EmailAddressValidator(MultiMap parameters) {
		super(parameters, "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
	}

	protected ValidationError createError(ValidationContext context, String value) {
		return new ValidationError("emailAddress.value.invalid", context, value);
	}

}