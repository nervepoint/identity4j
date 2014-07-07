/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.Collections;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value is a valid
 * {@link java.lang.Integer} and conforms to the supplied {@link #MINIMUM_VALUE}
 * and {@link #MAXIMUM_VALUE}. If the {@link #MINIMUM_VALUE} or
 * {@link #MAXIMUM_VALUE} is not specified, the validator uses the default
 * values.
 */
public class IntegerValidator extends AbstractSingleValueValidator {
    /**
     * Parameter which when supplied, specifies the minimum value for the
     * {@link java.lang.Integer}. If this value is not supplied, the validator
     * defaults to a minimum value of 0.
     */
    public static final String MINIMUM_VALUE = "MINIMUM_VALUE";
    /**
     * Parameter which when supplied, specifies the maximum value for the
     * {@link java.lang.Integer}. If this value is not supplied, the validator
     * defaults to a maximum selection of {@link java.lang.Integer#MAX_VALUE}.
     */
    public static final String MAXIMUM_VALUE = "MAXIMUM_VALUE";

    private final int minimumValue;
    private final int maximumValue;

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public IntegerValidator(MultiMap parameters) {
        super(parameters);
        minimumValue = parameters.getIntegerOrDefault(MINIMUM_VALUE, 0);
        maximumValue = parameters.getIntegerOrDefault(MAXIMUM_VALUE, Integer.MAX_VALUE);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        try {
            Integer valueOf = Integer.valueOf(value);
            if (valueOf < minimumValue) { 
                return Collections.singleton(new ValidationError("integer.value.too.small", context, valueOf, minimumValue));
            } else if (valueOf > maximumValue) {
                return Collections.singleton(new ValidationError("integer.value.too.large", context, valueOf, maximumValue));
            }
            return Collections.emptyList();
        } catch (NumberFormatException nfe) {
            return Collections.singleton(new ValidationError("integer.value.invalid", context, value));
        }
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("[minimumValue='").append(minimumValue);
        builder.append("', maximumValue='").append(maximumValue).append("']");
        return builder.toString();
    }

	@Override
	public boolean isValueRequired() {
		return true;
	}
}