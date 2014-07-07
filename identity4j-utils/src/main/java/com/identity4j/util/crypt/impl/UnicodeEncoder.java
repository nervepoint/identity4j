package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.EncoderException;

public class UnicodeEncoder extends AbstractEncoder {

	public final static String ID = "unicode";

	public UnicodeEncoder() {
		super(ID);
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			String newQuotedPassword = "\"" + new String(toEncode, charset) + "\"";
			char[] unicodePwd = newQuotedPassword.toCharArray();
			byte pwdArray[] = new byte[unicodePwd.length * 2];
			for (int i = 0; i < unicodePwd.length; i++) {
				pwdArray[i * 2 + 1] = (byte) (unicodePwd[i] >>> 8);
				pwdArray[i * 2 + 0] = (byte) (unicodePwd[i] & 0xff);
			}
			return pwdArray;
		} catch (UnsupportedEncodingException e) {
			return new byte[0];
		}
	}
}
