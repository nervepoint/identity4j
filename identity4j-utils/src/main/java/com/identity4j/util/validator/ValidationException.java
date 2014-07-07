/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.identity4j.util.i18n.Messages;

/**
 * Exception thrown when a field fails to validate.
 */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = -7157434556969163365L;
    private final ValidationError validationError;
    private final Map<String, Collection<ValidationError>> validatorErrors = new HashMap<String, Collection<ValidationError>>();

    /**
     * Constructor for creating an exception from a single {@link ValidationError}.
     * 
     * @param validationError error
     */
    public ValidationException(ValidationError validationError) {
        super(validationError.getMessage());
        this.validationError = validationError;
    }

    /**
     * Constructor
     * 
     * @param bundle resource bundle name
     * @param message message key
     * @param args message arguments
     */
    public ValidationException(ValidationContext context, String bundle, String message, Object... args) {
        this(new ValidationError(bundle, context, message, args));
    }

    /**
     * Constructor
     * 
     * @param bundle resource bundle name
     * @param message message key
     * @param cause cause
     * @param args message arguments
     */
    public ValidationException(ValidationContext context, String bundle, String message, Throwable cause, Object... args) {
        super(message, cause);
        validationError = new ValidationError(bundle, context, message, args);
    }

    /**
     * Constructor for creating an exception from a collection of {@link ValidationError}.
     * 
     * @param validatorErrors list of errors
     */
    public ValidationException(ValidationContext context, Map<String, Collection<ValidationError>> validatorErrors, Object... args) {
        this(context, ValidationError.RESOURCE_BUNDLE_VALIDATOR, "muliple.validation.failures", args);
        this.validatorErrors.putAll(validatorErrors);
    }
    
    public String getLocalizedMessage(ClassLoader classLoader) {
        return getLocalizedMessage(classLoader, Locale.getDefault());
    }

    public String getLocalizedMessage(ClassLoader classLoader, Locale locale) {
        return Messages.getString(classLoader, locale, validationError.getBundle(), validationError.getMessage(), validationError.getArgs());
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "." + validatorErrors.toString();
    }

    /**
     * Returns the validation error associated with this exception
     * 
     * @return the validation error
     */
    public final ValidationError getValidationError() {
        return validationError;
    }

    /**
     * Returns the validation errors associated with this exception
     * 
     * @return the validation errors
     */
    public Map<String, Collection<ValidationError>> getValidatorErrors() {
        return validatorErrors;
    }
}