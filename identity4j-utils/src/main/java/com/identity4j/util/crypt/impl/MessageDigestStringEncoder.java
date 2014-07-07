package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.EncoderException;

public class MessageDigestStringEncoder extends CompoundEncoder {

	private String hashAlgorithm;

	public MessageDigestStringEncoder(String id, String encryptionEncoderId, String hashAlgorithm) {
		super(id);
		addEncoder(new MessageDigestEncoder(encryptionEncoderId, hashAlgorithm));
		addEncoder(new Base64Encoder());
		this.hashAlgorithm = hashAlgorithm;
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			return ("{" + hashAlgorithm + "}" + new String(super.encode(toEncode, salt, passphrase, charset), charset))
				.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		try {
			return new String(encodedBytes, charset).startsWith("{" + hashAlgorithm + "}");
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

}
