package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.unix.DESCrypt;

public class UnixDESEncoder extends AbstractEncoder {

	public static final String ID = "unix-des";

	public UnixDESEncoder() {
		super(ID);
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) throws EncoderException {
		try {
			String encoded = new String(encodedData, charset);
			if(encoded.equals("*")) {
				// No login
				return false;
			}
			if(encoded.startsWith("!")) {
				// Password locked
				return false;
			}
			String encsalt = encoded.substring(0, 2);
			return Arrays.equals(encode(unencodedData, encsalt.getBytes(charset), passphrase, charset), encodedData);

		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		return false;
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (salt == null) {
				salt = new byte[] { gensalt(), gensalt() };
			} else {
				if (salt.length != 2) {
					throw new IllegalArgumentException("Salt must be two characters");
				}
			}
			String saltString = new String(salt, charset);
			return (saltString + DESCrypt.crypt(saltString, new String(toEncode, charset))).getBytes(charset);
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}

	private byte gensalt() {
		return (byte) ((Math.random() * 76) + 46);
	}
}
