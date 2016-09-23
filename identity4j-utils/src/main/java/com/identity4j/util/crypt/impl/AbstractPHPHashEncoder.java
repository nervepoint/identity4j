package com.identity4j.util.crypt.impl;

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
