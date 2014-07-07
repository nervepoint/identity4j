/* HEADER */
package com.identity4j.util.validator;

import java.util.Collection;
import java.util.Collections;

import javax.naming.NamingException;
import javax.naming.ldap.LdapName;

import com.identity4j.util.MultiMap;

/**
 * A validator implementation that checks the supplied value conforms to the
 * rules set out by the {@link java.net.URL} implementation.
 */
public class DnValidator extends AbstractSingleValueValidator {

    /**
     * Constructor.
     *
     * @param parameters parameters
     */
    public DnValidator(MultiMap parameters) {
        super(parameters);
    }

    @Override
    final Collection<ValidationError> validate(ValidationContext context, String value) {
        try {
    		new LdapName(value);
        } catch (NamingException mue) {
            return Collections.singleton(new ValidationError("dn.value.invalid", context, value));
        }
        return Collections.emptyList();
    }
}