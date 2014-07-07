package com.identity4j.util.crypt.impl;

import org.apache.commons.codec.binary.Base64;

import com.identity4j.util.crypt.EncoderException;

public class Base64Encoder extends AbstractEncoder {

	public static final String ID = "base64";

	public Base64Encoder() {
		super(ID);
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		Base64 encoder = new Base64(0, null, false);
		return encoder.encode(toEncode);
	}

	@Override
	public byte[] decode(byte[] toDecode, byte[] passphrase, String charset) throws EncoderException {
		Base64 encoder = new Base64();
		return encoder.decode(toDecode);
	}

}
