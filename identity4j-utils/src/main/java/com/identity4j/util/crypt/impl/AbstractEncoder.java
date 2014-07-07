package com.identity4j.util.crypt.impl;

import java.util.Arrays;

import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.EncoderException;

public abstract class AbstractEncoder implements Encoder {

	private String id;

	public AbstractEncoder(String id) {
		this.id = id;
	}

	@Override
	public byte[] decode(byte[] toDecode, byte[] passphrase, String charset) throws EncoderException {
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

}
