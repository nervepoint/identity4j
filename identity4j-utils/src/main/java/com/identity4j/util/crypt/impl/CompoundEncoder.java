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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.EncoderException;

public class CompoundEncoder extends AbstractEncoder {
	private List<Encoder> encoders = new ArrayList<Encoder>();

	public CompoundEncoder(String id, Encoder... encoders) {
		super(id);
		this.encoders.addAll(Arrays.asList(encoders));
	}

	public void addEncoder(Encoder encoder) {
		encoders.add(encoder);
	}

	@Override
	public byte[] decode(byte[] toDecode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		List<Encoder> reverse = new ArrayList<Encoder>(encoders);
		Collections.reverse(reverse);
		for (Encoder encoder : reverse) {
			toDecode = encoder.decode(toDecode, null, passphrase, charset);
		}
		return toDecode;
	}

	@Override
    public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) {
        for (int i = encoders.size() - 1 ; i >= 0 ; i--) {
            Encoder enc = encoders.get(i);
            if(i == 0)
                return enc.match(encodedData, unencodedData, passphrase, charset);
            else {
                encodedData = enc.decode(encodedData, null, passphrase, charset);
            }
        }
        return false;
    }

    @Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		for (Encoder encoder : encoders) {
			toEncode = encoder.encode(toEncode, salt, passphrase, charset);
		}
		return toEncode;
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		for (Encoder encoder : encoders) {
			if (!encoder.isOfType(encodedBytes, charset)) {
				return false;
			}
		}
		return true;
	}

}
