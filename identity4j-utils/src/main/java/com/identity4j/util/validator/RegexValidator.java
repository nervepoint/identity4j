/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value matches the given
 * {@link java.util.regex.Pattern}. If the {@link #REGEX} is not specified, the
 * validator uses the default value.
 */
public class RegexValidator extends AbstractSingleValueValidator {
	/**
	 * Parameter which when supplied, specifies the
	 * {@link java.util.regex.Pattern} valid for the string.
	 */
	public static final String REGEX = "REGEX";
	public static final String REGEX_FLAGS = "REGEX_FLAGS";
	private final String regex;
	private Pattern pattern;

	public RegexValidator(MultiMap parameters, String regex, int flags) {
		super(parameters);
		this.regex = regex;
		pattern = Pattern.compile(regex, flags);
	}

	/**
	 * Constructor.
	 * 
	 * @param parameters parameters
	 */
	public RegexValidator(MultiMap parameters) {
		super(parameters);
		regex = parameters.getStringOrDefault(REGEX, ".*");
		pattern = Pattern.compile(regex, parameters.getIntegerOrDefault(REGEX_FLAGS, 0));
	}

	@Override
	final Collection<ValidationError> validate(ValidationContext context, String value) {
		if (pattern.matcher(value).matches()) {
			return Collections.emptyList();
		}
		return Collections.singleton(createError(context, value));
	}

	protected ValidationError createError(ValidationContext context, String value) {
		return new ValidationError("regex.pattern.does.not.match", context, value, regex);
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[regex='").append(regex).append("']");
		return builder.toString();
	}
}