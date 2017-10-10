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

import com.identity4j.util.crypt.EncoderException;

public abstract class AbstractUnixSHAEncoder extends AbstractEncoder {

	private final String saltPrefix;
	@SuppressWarnings("unused")
	private final int size;

	public AbstractUnixSHAEncoder(String id, String saltPrefix, int size) {
		super(id);
		this.saltPrefix = saltPrefix;
		this.size = size;
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (!isOfType(encodedData, charset)) {
				throw new EncoderException("Encoded data is not in Unix SHA crypt format");
			}
			String encoded = new String(encodedData, charset);
			if(encoded.equals("*")) {
				// No login
				return false;
			}
			if(encoded.startsWith("!")) {
				// Password locked
				return false;
			}
			int sl = saltPrefix.length();
			int idx = encoded.indexOf('$', sl);
			if (idx == -1) {
				throw new EncoderException("Expected end of salt character $");
			}
			String encsalt = encoded.substring(0, idx);
			String toMatch = doCrypt(unencodedData, charset, encsalt);
			return toMatch.equals(encoded);
			
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

    protected abstract String doCrypt(byte[] unencodedData, String charset, String salt) throws UnsupportedEncodingException;

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		try {
			return new String(encodedBytes, charset).startsWith(saltPrefix);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (salt == null) {
	            return doCrypt(toEncode, charset, null).getBytes(charset);
			} else {
				final String saltString = new String(salt, charset);
				if (saltString.length() < 2) {
					throw new EncoderException("Salt must be at least 2 characters .");
				}
				final String crypt = doCrypt(toEncode, charset, saltString);
				return crypt.getBytes(charset);
			    
			}
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}
}
