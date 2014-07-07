package com.identity4j.connector.unix;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.crypt.impl.AbstractEncoder;

public class AbstractUnixSHAEncoder extends AbstractEncoder {

	private final String saltPrefix;
	private final int size;

	public AbstractUnixSHAEncoder(String id, String saltPrefix, int size) {
		super(id);
		this.saltPrefix = saltPrefix;
		this.size = size;
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (!isOfType(encodedData, charset)) {
				throw new EncoderException("Encoded data is not in Unix SHA crypt format");
			}
			String encoded = new String(encodedData, charset);
			int sl = saltPrefix.length();
			int idx = encoded.indexOf('$', sl);
			if (idx == -1) {
				throw new EncoderException("Expected end of salt character $");
			}
			String salt = encoded.substring(0, idx);
			return Arrays.equals(encode(unencodedData, salt.getBytes(charset), passphrase, charset), encodedData);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public boolean isOfType(byte[] encodedBytes, String charset) {
		try {
			return new String(encodedBytes, charset).startsWith(saltPrefix);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (salt == null) {
				return SHACrypt.shaCrypt(new String(toEncode, charset), charset, size).getBytes(charset);
			} else {
				final String saltString = new String(salt, charset);
				if (saltString.length() < 2) {
					throw new EncoderException("Salt must be at least 2 characters .");
				}
				final String crypt = SHACrypt.crypt_sha(toEncode, saltString, size);
				return crypt.getBytes(charset);
			}
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}
}
