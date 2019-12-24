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
import java.util.Collections;

import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

/**
 * An abstract validator implementation that provides default behaviour.
 */
public abstract class AbstractSingleValueValidator implements Validator {
    /**
     * Parameter which when supplied, specifies if each value should be
     * preserved or trimmed ({@link java.lang.String#trim()}). If this value is
     * not supplied, the validator trims each value.
     */
    public static final String TRIM = "TRIM";
    
    /**
     * Parameter hinting a value must be supplied.
     */
    public static final String REQUIRED = "REQUIRED";
    
    private final boolean trim;
	private boolean required;

    /**
     * Constructor.
     * 
     * @param parameters parameters
     */
    public AbstractSingleValueValidator(MultiMap parameters) {
        trim = parameters.getBooleanOrDefault(TRIM, true);
        required = parameters.getBooleanOrDefault(REQUIRED, false);  
    }

    public final Collection<ValidationError> validate(ValidationContext context, String... values) {
        if (values == null) {
            throw new IllegalArgumentException("Null values provided");
        }

        Collection<ValidationError> results = new ArrayList<ValidationError>();
        if (values.length == 0) {
            // if no values are provided, is an empty value valid?
            results.addAll(fixAndValidate(""));
        } else {
            for (String value : values) {
                results.addAll(fixAndValidate(value));
            }
        }
        return results;
    }

    @Override
	public boolean isValueRequired() {
		return required;
	}

	private Collection<ValidationError> fixAndValidate(String value) {
        String nonNull = StringUtil.nonNull(value);
        String trimmedValue = trim ? nonNull.trim() : nonNull;
        if(trimmedValue.equals("") && !isValueRequired()) {
        	return Collections.emptyList();
        }
        return validate(null, trimmedValue);
    }

    abstract Collection<ValidationError> validate(ValidationContext context, String value);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("[trim='").append(trim).append("']");
        return builder.toString();
    }
}