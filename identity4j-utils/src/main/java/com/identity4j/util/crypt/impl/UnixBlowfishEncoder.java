package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.unix.BCrypt;

public class UnixBlowfishEncoder extends AbstractEncoder {

	public static final String ID = "unix-blowfish";

	public UnixBlowfishEncoder() {
		super(ID);
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (!isOfType(encodedData, charset)) {
				throw new EncoderException("Encoded data is not in Unix MD5 crypt format");
			}
			String encoded = new String(encodedData);
			if(encoded.equals("*")) {
				// No login
				return false;
			}
			if(encoded.startsWith("!")) {
				// Password locked
				return false;
			}
			return BCrypt.checkpw(new String(unencodedData, charset), encoded);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		try {
			final String string = new String(encodedBytes, charset);
			return string.startsWith("$2$") || string.startsWith("$2a$");
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			String saltString = new String(salt == null ? BCrypt.gensalt().getBytes(charset) : salt);
			return BCrypt.hashpw(new String(toEncode, charset), saltString).getBytes(charset);
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}
}
