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


import java.util.ArrayList;
import java.util.Collection;

import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

/**
 * A validator implementation that checks the supplied values conform to the
 * supplied {@link #MINIMUM_SELECTION} and {@link #MAXIMUM_SELECTION} and the
 * values are present in the supplied {@link #LIST_CONTENTS}. If the
 * {@link #MINIMUM_SELECTION}, {@link #MAXIMUM_SELECTION} or
 * {@link #LIST_CONTENTS} is not specified, the validator uses the default
 * values.
 */
public class ListValidator implements Validator {
    /**
     * Parameter which when supplied, specifies the contents of the list. If
     * this value is not supplied, the validator does not check the list
     * contents.
     */
    public static final String LIST_VALUES = "LIST_VALUES";
    /**
     * Parameter which when supplied, specifies the minimum selection for the
     * list. If this value is not supplied, the validator defaults to a minimum
     * selection of 0.
     */
    public static final String MINIMUM_SELECTION = "MINIMUM_SELECTION";
    /**
     * Parameter which when supplied, specifies the maximum selection for the
     * list. If this value is not supplied, the validator defaults to a maximum
     * selection of 1.
     */
    public static final String MAXIMUM_SELECTION = "MAXIMUM_SELECTION";
    /**
     * Parameter which when supplied, specifies if each value should be
     * preserved or trimmed ({@link java.lang.String#trim()}). If this value is
     * not supplied, the validator trims each value.
     */
    public static final String TRIM = "TRIM";

    private final Collection<String> listContents;
    private final int minimumSelection;
    private final int maximumSelection;
    private final boolean trim;

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public ListValidator(MultiMap parameters) {
        listContents = StringUtil.toDefaultList(parameters.getStringOrDefault(LIST_VALUES, ""));
        minimumSelection = parameters.getIntegerOrDefault(MINIMUM_SELECTION, 0);
        maximumSelection = parameters.getIntegerOrDefault(MAXIMUM_SELECTION, 1);
        trim = parameters.getBooleanOrDefault(TRIM, true);
    }

    public final Collection<ValidationError> validate(ValidationContext context, String... values) {
        if (values == null) {
            throw new IllegalArgumentException("Null values provided");
        }
        Collection<ValidationError> errors = new ArrayList<ValidationError>();
        if (values.length < minimumSelection) {
            errors.add(new ValidationError("list.minimum.selection", context, minimumSelection));
        } else if (values.length > maximumSelection) {
            errors.add(new ValidationError("list.maximum.selection", context, maximumSelection));
        }
        if (!listContents.isEmpty()) {
            for (String value : values) {
                String nonNull = StringUtil.nonNull(value);
                String trimmedValue = trim ? nonNull.trim() : nonNull;
                if (!listContents.contains(trimmedValue)) {
                    errors.add(new ValidationError("list.invalid.selection", context, trimmedValue));
                }
            }
        }
        return errors;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("[listContents='").append(listContents);
        builder.append("', minimumSelection='").append(minimumSelection);
        builder.append("', maximumSelection='").append(maximumSelection).append("']");
        return builder.toString();
    }

	@Override
	public boolean isValueRequired() {
		return minimumSelection > 0;
	}
}