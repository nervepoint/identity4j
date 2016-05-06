package com.identity4j.connector.unix;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.crypt.impl.AbstractEncoder;

public class UnixMD5Encoder extends AbstractEncoder {

	public static final String ID = "unix-md5";

	private final String saltPrefix;

	public UnixMD5Encoder() {
		this(ID, "$1$");
	}

	public UnixMD5Encoder(String id, String saltPrefix) {
		super(id);
		this.saltPrefix = saltPrefix;
	}

	@Override
	public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) throws EncoderException {
		try {
			if (!isOfType(encodedData, charset)) {
				throw new EncoderException("Encoded data is not in Unix MD5 crypt format");
			}
			String encoded = new String(encodedData, charset);
			if(encoded.equals("*")) {
				// No login
				return false;
			}
			if(encoded.startsWith("!")) {
				// Password locked
				return false;
			}
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
				return MD5Crypt.md5Crypt(new String(toEncode, charset), charset).getBytes(charset);
			} else {
				final String saltString = new String(salt, charset);
				if (saltString.length() < 2) {
					throw new EncoderException("Salt must be at least 2 characters .");
				}
				final String crypt = MD5Crypt.crypt_md5(toEncode, saltString, saltPrefix);
				return crypt.getBytes(charset);
			}
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}
}
