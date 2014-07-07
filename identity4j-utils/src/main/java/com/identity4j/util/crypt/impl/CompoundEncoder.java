package com.identity4j.util.crypt.impl;

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
	public byte[] decode(byte[] toDecode, byte[] passphrase, String charset) throws EncoderException {
		List<Encoder> reverse = new ArrayList<Encoder>(encoders);
		Collections.reverse(reverse);
		for (Encoder encoder : reverse) {
			toDecode = encoder.decode(toDecode, passphrase, charset);
		}
		return toDecode;
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
