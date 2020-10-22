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

public class SHABase64EncoderTest extends AbstractEncoderTest {

    public SHABase64EncoderTest() throws UnsupportedEncodingException {
        super(SHABase64Encoder.ID, new byte[][] { "rDszI4Mgv2OXvvUWJukxE9AJuGA=".getBytes("UTF-8"),
                        "RLxtGXHPx8qFzPH6Az3QzUI5WKU=".getBytes("UTF-8"),
                        "HvGpWakY1gTem9qtNdR1ij9H4Uw=".getBytes("UTF-8") }, null,null,  false, false);
    }

}
