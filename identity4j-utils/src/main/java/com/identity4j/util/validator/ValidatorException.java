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


/**
 * Exception thrown when a {@link Validator} problem occurs. This is not
 * intended to be thrown from a validator when validation fails, but instead is
 * to be used to instead signify a problem with the validator itself.
 * 
 * As this class is a runtime exception, there is no need for user code to catch
 * it or subclasses if any error is to be considered fatal (the usual case).
 */
public class ValidatorException extends RuntimeException {
    private static final long serialVersionUID = -7157434556969163365L;

    /**
     * Constructor.
     *
     * @param message message
     */
    public ValidatorException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param cause cause
     */
    public ValidatorException(String message, Throwable cause) {
        super(message, cause);
    }
}