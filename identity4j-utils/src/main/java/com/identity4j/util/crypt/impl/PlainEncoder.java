package com.identity4j.util.crypt.impl;

import com.identity4j.util.crypt.EncoderException;

public class PlainEncoder extends AbstractEncoder {

	public final static String ID = "plain";

	public PlainEncoder() {
		super(ID);
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		return toEncode;
	}

	@Override
	public byte[] decode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		byte[] decoded = new byte[toEncode.length];
		System.arraycopy(toEncode, 0, decoded, 0, decoded.length);
		return decoded;
	}
}
