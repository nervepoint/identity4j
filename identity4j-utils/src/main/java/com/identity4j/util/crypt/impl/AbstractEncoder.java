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

import java.util.Arrays;

import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.EncoderException;

public abstract class AbstractEncoder implements Encoder {

	private String id;

	public AbstractEncoder(String id) {
		this.id = id;
	}

	@Override
	public byte[] decode(byte[] toDecode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		return false;
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) {
		return Arrays.equals(encodedData, encode(unencodedData, null, passphrase, charset));
	}


    protected byte[] randomBytes(int count) {
        byte[] b = new byte[count];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (Math.random() * 256f);
        }
        return b;
    }
}
