package com.identity4j.util.crypt.impl;

import com.identity4j.util.crypt.EncoderManager;

public class DefaultEncoderManager extends EncoderManager {

	private final static EncoderManager INSTANCE = new DefaultEncoderManager();

	private DefaultEncoderManager() {
		try {
			addEncoder(new PlainEncoder());
			addEncoder(new Base64Encoder());
			addEncoder(new UnicodeEncoder());
			addEncoder(new MD5Encoder());
			addEncoder(new SHAEncoder());
			addEncoder(new MD5Base64Encoder());
			addEncoder(new SHABase64Encoder());
			addEncoder(new MD5StringEncoder());
			addEncoder(new SHAStringEncoder());
			addEncoder(new PBEWithMD5AndDESEncoder());
			addEncoder(new Base64PBEWithMD5AndDESEncoder());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static EncoderManager getInstance() {
		return INSTANCE;
	}

}
