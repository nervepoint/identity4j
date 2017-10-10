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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.identity4j.util.crypt.EncoderException;

public class MessageDigestEncoder extends AbstractEncoder {

	private String type;

	public MessageDigestEncoder(String id, String type) {
		super(id);
		this.type = type;
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			MessageDigest digest = MessageDigest.getInstance(type);
			digest.reset();
			digest.update(toEncode);
			return digest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new EncoderException(e);
		}
	}
}
