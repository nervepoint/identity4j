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


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.junit.Test;

import com.identity4j.util.MultiMap;

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