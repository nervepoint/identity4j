/* HEADER */
package com.identity4j.util.validator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;
import com.identity4j.util.validator.IpAddressValidator;
import com.identity4j.util.validator.ValidationError;
import com.identity4j.util.validator.Validator;

public class IpAddressValidatorTest extends AbstractSingleValueValidatorTest {

    public IpAddressValidatorTest() {
        super(
                        "127.0.0.1:8080",
                        "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                                        + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
                                        + "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    }

    @Test
    public final void invalidPort() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, "127.0.0.1:");
        assertEquals(1, validate.size());
    }

    @Test
    public final void outsideLowerPort() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, "127.0.0.1:-1");
        assertEquals(1, validate.size());
    }

    @Test
    public final void outsideUpperPort() {
        Validator validator = createValidator();
        Collection<ValidationError> validate = validator.validate(null, "127.0.0.1:65536");
        assertEquals(1, validate.size());
    }

    @Test
    public final void isHostName() {
        assertTrue("Should be hostName", IpAddressValidator.isHostName("local.nervepoint.com"));
    }

    @Test
    public final void isHostNameLooksLikeIpAddress() {
        assertTrue("Should be hostName", IpAddressValidator.isHostName("a192.168.0.1"));
    }

    @Test
    public final void isIpAddress() {
        assertFalse("Should not be hostName", IpAddressValidator.isHostName("192.168.0.1"));
    }

    @Test
    public final void isIpv6Address() {
        assertFalse("Should not be hostName", IpAddressValidator.isHostName("3ffe:1900:4545:3:200:f8ff:fe21:67cf"));
    }

    @Override
    protected Map<String, String[]> createValidatorParameterMap() {
        Map<String, String[]> parameters = super.createValidatorParameterMap();
        parameters.put(IpAddressValidator.INCLUDES_PORT, new String[] { "true" });
        return parameters;
    }

    @Override
    protected Validator createValidator(MultiMap multiMap) {
        return new IpAddressValidator(multiMap);
    }
}