package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.Base64PBEWithMD5AndDESEncoder;

public class Base64PBEWithMD5AndDESEncoderTest extends AbstractEncoderTest {

	public Base64PBEWithMD5AndDESEncoderTest() throws UnsupportedEncodingException {
		super(Base64PBEWithMD5AndDESEncoder.ID, true, false);
		setExpectedHashes(new byte[][] { "CBWMo0pmUSq8EJT3y+UXfq0=".getBytes("UTF-8"),
			"CBWMo0pmUSq8LJFjdF91CJRgxiulYsq2GR0GdKs+SmuA6icnA5fGeL8=".getBytes("UTF-8"), "CBWMo0pmUSq8zwRZTkZDprHJt8byrXTGOSl3e7iQB5Wx7D2haUQaHUdDe+y7q1hv5ffvdID2YkGW".getBytes("UTF-8") });
		setPassphrases(new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
	}

}
