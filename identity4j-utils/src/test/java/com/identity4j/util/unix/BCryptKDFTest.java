package com.identity4j.util.unix;

import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Test;

public class BCryptKDFTest {

	@Test
	public void test() throws NoSuchAlgorithmException {
		Assert.assertArrayEquals(
				new byte[] { 22, 121, (byte) 130, (byte) 247, (byte) 229, 39, 91, (byte) 149, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, },
				BCryptKDF.bcrypt_pbkdf("password".getBytes(), "drowssap".getBytes(), "12345678".getBytes(), 100));

	}
}
