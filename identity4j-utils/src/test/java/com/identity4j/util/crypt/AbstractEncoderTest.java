package com.identity4j.util.crypt;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import com.identity4j.util.crypt.impl.DefaultEncoderManager;

public abstract class AbstractEncoderTest {
	
	static {
		BasicConfigurator.configure();
	}
	
	private final Encoder encoder;
	private final boolean twoWay;
	protected String[] testStrings;
	private byte[][] expectedHashes;
	private byte[][] salts;
	private byte[][] passphrases;
	
	private final static String[] TEST_STRINGS = { "asecret", "a slightly longer secret",
		"a secret with other characters like $£\"!&*(" };

	public AbstractEncoderTest(String encoderId, boolean twoWay, boolean canRecogniseType) {
		this(encoderId, TEST_STRINGS, null, null, null, twoWay, canRecogniseType);
	}

	public AbstractEncoderTest(String encoderId, byte[][] expectedHashes, byte[][] salts, byte[][] passphrases, boolean twoWay,
			boolean canRecogniseType) {
		this(encoderId, TEST_STRINGS, expectedHashes, salts, passphrases, twoWay, canRecogniseType);
	}

	public AbstractEncoderTest(String encoderId, String[] testStrings, byte[][] expectedHashes, byte[][] salts,
			byte[][] passphrases, boolean twoWay, boolean canRecogniseType) {
		this.salts = salts;
		this.passphrases = passphrases;
		this.twoWay = twoWay;
		this.testStrings = testStrings;
		this.expectedHashes = expectedHashes;
		encoder = DefaultEncoderManager.getInstance().getEncoderById(encoderId);
		assertNotNull(encoder);
	}

	protected final void setExpectedHashes(byte[][] expectedHashes) {
		this.expectedHashes = expectedHashes;
	}

	protected final void setSalts(byte[][] salts) {
		this.salts = salts;
	}

	protected final void setPassphrases(byte[][] passphrases) {
		this.passphrases = passphrases;
	}

	@Test
	public void testEncode() throws UnsupportedEncodingException, EncoderException {
		int i = 0;
		for (String string : testStrings) {
			byte[] expected = expectedHashes[i];
			byte[] actual = encoder.encode(string.getBytes("UTF-8"), salts == null ? null : salts[i], passphrases == null ? null
				: passphrases[i], "UTF-8");
			System.out.println("Expected '" + new String(expected) + "'");
			System.out.println("Got      '" + new String(actual) + "'");
			assertArrayEquals(expected, actual);
			i++;
		}
	}

	@Test
	public void testDecode() throws UnsupportedEncodingException, EncoderException {
		if (twoWay) {
			int i = 0;
			for (String string : testStrings) {
				byte[] expected = string.getBytes("UTF-8");
				byte[] decoded = encoder.decode(expectedHashes[i], null, passphrases == null ? null : passphrases[i], "UTF-8");
				assertArrayEquals(expected, decoded);
				i++;
			}
		} else {
			try {
				encoder.decode(expectedHashes[0], null, passphrases == null ? null : passphrases[0], "UTF-8");
				assertTrue("UnsupportedOperationException must be thrown", false);
			} catch (UnsupportedOperationException uoe) {
				// Ok
			}
		}
	}

	// TODO what's going on here
	// @Test
	// public void testIsOfType() throws UnsupportedEncodingException,
	// EncoderException {
	// if (canRecogniseType) {
	// int i = 0;
	// for (String string : TEST_STRINGS) {
	// assertTrue(encoder.isOfType(expectedHashes[i++], "UTF-8"));
	// }
	// }
	// }

	@Test
	public void testMatch() throws UnsupportedEncodingException, EncoderException {
		int i = 0;
		for (String string : testStrings) {
			assertTrue(encoder.match(expectedHashes[i], string.getBytes("UTF-8"), passphrases == null ? null : passphrases[i],
				"UTF-8"));
			i++;
		}
	}
}
