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

/**
 * There are many methods of converting passwords and other confidential
 * information for storage. This interfaces is to be implemented by classes that
 * may be used to encode some text into a byte array, and optionally back again.
 */
public interface Encoder {

	/**
	 * Get the ID of this encoder.
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Determine if this encoder may have been used to create the provided
	 * encoded bytes. This method should only be used if it is <b>very
	 * likely</b> to be the right encoder.
	 * 
	 * @param encodedBytes encoded bytes
	 * @param charset character set
	 * @return this almost certainly was encoded with a similar encoder
	 */
	boolean isOfType(byte[] encodedBytes, String charset);

	/**
	 * Encode a string
	 * 
	 * @param toEncode data to encode
	 * @param salt salt if supported
	 * @param passphrase passphrase if supported
	 * @param charset character set
	 * @return encoded bytes
	 * @throws EncoderException if encoding fails
	 */
	byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException;

	/**
	 * Decode bytes (if supported) to text.
	 * 
	 * @param toDecode bytes to decode
	 * @param salt TODO
	 * @param passphrase passphrase if supported
	 * @param charset character set
	 * @return decoded text
	 * @throws EncoderException if encoding fails
	 * @throws UnsupportedOperationException
	 */
	byte[] decode(byte[] toDecode, byte[] salt, byte[] passphrase, String charset) throws EncoderException;

	/**
	 * Check if the provide un-encoded data would match the encoded data if it
	 * was encoded.
	 * 
	 * @param encodedData encoded data
	 * @param unencodedData un-encoded data
	 * @param passphrase passphrase if supported
	 * @param charset character set
	 * @return matches
	 */
	boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset);
}
