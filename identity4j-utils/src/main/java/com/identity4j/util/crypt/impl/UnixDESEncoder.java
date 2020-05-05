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
import java.util.Arrays;

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.unix.DESCrypt;

public class UnixDESEncoder extends AbstractEncoder {

	public static final String ID = "unix-des";

	public UnixDESEncoder() {
		super(ID);
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) throws EncoderException {
		try {
			String encoded = new String(encodedData, charset);
			if(encoded.equals("*")) {
				// No login
				return false;
			}
			if(encoded.startsWith("!")) {
				// Password locked
				return false;
			}
			String encsalt = encoded.substring(0, 2);
			return Arrays.equals(encode(unencodedData, encsalt.getBytes(charset), passphrase, charset), encodedData);

		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		return false;
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (salt == null) {
				salt = new byte[] { gensalt(), gensalt() };
			} else {
				if (salt.length != 2) {
					throw new IllegalArgumentException("Salt must be two characters");
				}
			}
			String saltString = new String(salt, charset);
			return DESCrypt.crypt(saltString, new String(toEncode, charset)).getBytes(charset);
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}

	private byte gensalt() {
		return (byte) ((Math.random() * 76) + 46);
	}
}
