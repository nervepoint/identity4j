package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.SHAStringEncoder;

public class SHAStringEncoderTest extends AbstractEncoderTest {

	public SHAStringEncoderTest() throws UnsupportedEncodingException {
		super(SHAStringEncoder.ID, new byte[][] { "{SHA}rDszI4Mgv2OXvvUWJukxE9AJuGA=".getBytes("UTF-8"),
			"{SHA}RLxtGXHPx8qFzPH6Az3QzUI5WKU=".getBytes("UTF-8"), "{SHA}HvGpWakY1gTem9qtNdR1ij9H4Uw=".getBytes("UTF-8") }, null,
			null, false, true);
	}

}
