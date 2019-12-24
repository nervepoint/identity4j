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

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AES256EncoderTest extends AbstractEncoderTest {

    public Base64AES256EncoderTest() throws UnsupportedEncodingException {
        super(Base64AES256Encoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AQAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAACkPb/70IlxmVFGTSVRf0ru".getBytes("UTF-8"),
                        "AQAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAABGDuQekGpvwYuEDFpdFo5juU6O8Pd1OtVp8894THBdYw==".getBytes("UTF-8"),
                        "AQAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAAANMzrz7qeHdM7YV2fowUV/tO8qm9hzdxhR1xPjf+qlerJDSlyU6PGc2Lg8gyVtnQo=".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
