package com.identity4j.util.crypt.impl;

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
