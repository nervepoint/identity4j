package com.identity4j.connector.unix;

/*
 * #%L
 * Identity4J Unix
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

import org.junit.Ignore;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.UnixSHA512Encoder;

@Ignore
// TODO This encoder doesn't work yet
public class UnixSHA512EncoderTest extends AbstractEncoderTest {

	final static String PW1 = "Hello world!";
	final static String HASH1 = "$6$saltstring$svn8UoSVapNtMuq1ukKS4tPQd8iKwSMHWjl/O817G3uBnIFNjnQJuesI68u4OTLiBFdcbYEdFCoEOfaS35inz1";
	final static String SALT1 = "$6$saltstring";

	public UnixSHA512EncoderTest() throws UnsupportedEncodingException {
		super(UnixSHA512Encoder.ID, new String[] { PW1 }, new byte[][] { HASH1.getBytes("UTF-8") }, new byte[][] { SALT1
			.getBytes("UTF-8") }, null, false, true);
	}

}
