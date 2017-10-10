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
import com.identity4j.util.crypt.impl.Base64PBEWithMD5AndDESEncoder;

public class Base64PBEWithMD5AndDESEncoderTest extends AbstractEncoderTest {

	public Base64PBEWithMD5AndDESEncoderTest() throws UnsupportedEncodingException {
		super(Base64PBEWithMD5AndDESEncoder.ID, true, false);
		setExpectedHashes(new byte[][] { "CBWMo0pmUSq8EJT3y+UXfq0=".getBytes("UTF-8"),
			"CBWMo0pmUSq8LJFjdF91CJRgxiulYsq2GR0GdKs+SmuA6icnA5fGeL8=".getBytes("UTF-8"), "CBWMo0pmUSq8zwRZTkZDprHJt8byrXTGOSl3e7iQB5Wx7D2haUQaHUdDe+y7q1hv5ffvdID2YkGW".getBytes("UTF-8") });
		setPassphrases(new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
	}

}
