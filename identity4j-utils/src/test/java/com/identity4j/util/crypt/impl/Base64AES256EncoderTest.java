package com.identity4j.util.crypt.impl;

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

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public class Base64AES256EncoderTest extends AbstractEncoderTest {

    private static final byte[][] TEST_KEYS;
    private static final byte[][] ENCODED_VECTORS;

    static {
        SecureRandom rng = new SecureRandom();
        TEST_KEYS = new byte[3][];
        for (int i = 0; i < 3; i++) {
            byte[] seed = new byte[12];
            rng.nextBytes(seed);
            TEST_KEYS[i] = Base64.getEncoder().encodeToString(seed).getBytes(StandardCharsets.UTF_8);
        }
        String[] testStrings = { "asecret", "a slightly longer secret",
            "a secret with other characters like $\u00a3\"!&*(" };
        try {
            Encoder enc = DefaultEncoderManager.getInstance().getEncoderById(Base64AES256Encoder.ID);
            byte[] empty = new byte[0];
            ENCODED_VECTORS = new byte[][] {
                enc.encode(testStrings[0].getBytes("UTF-8"), empty, TEST_KEYS[0], "UTF-8"),
                enc.encode(testStrings[1].getBytes("UTF-8"), empty, TEST_KEYS[1], "UTF-8"),
                enc.encode(testStrings[2].getBytes("UTF-8"), empty, TEST_KEYS[2], "UTF-8")
            };
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public Base64AES256EncoderTest() throws UnsupportedEncodingException {
        super(Base64AES256Encoder.ID, true, false);
        setPassphrases(TEST_KEYS);
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
        setExpectedHashes(ENCODED_VECTORS);
    }
}
