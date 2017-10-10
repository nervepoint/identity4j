package com.identity4j.util.crypt.impl;

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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import com.identity4j.util.crypt.EncoderException;

/**
 * Sort of emulate PHP's hash()
 */
public abstract class AbstractPHPHashEncoder extends AbstractEncoder {

    private final static Map<String, String> algoToNatives = new HashMap<String, String>();

    static {
        // TODO more
        algoToNatives.put("sha512", "SHA-512");
        algoToNatives.put("sha256", "SHA-256");
        algoToNatives.put("sha1", "SHA-1");
        algoToNatives.put("md5", "MD5");
    }

    public AbstractPHPHashEncoder(String id) {
        super(id);
    }

    protected byte[] hash(String algo, byte[] data) {
        return hash(algo, data, false);
    }

    protected byte[] hash(String algo, byte[] data, boolean raw) {
        return hash(algo, data, raw, Charset.defaultCharset().name());
    }

    protected byte[] hash(String algo, byte[] data, boolean raw, String charset) {
        try {

            String nalgo = algoToNative(algo);
            MessageDigest digest = MessageDigest.getInstance(nalgo);
            digest.reset();
            digest.update(data);
            byte[] digested = digest.digest();
            if (raw) {
                return digested;
            }
            else {
                return toHexits(digested).getBytes(charset);
            }
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    protected String toHexits(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    protected String algoToNative(String algo) {
        return algoToNatives.containsKey(algo) ? algoToNatives.get(algo) : algo;
    }


}
