package com.identity4j.util.crypt.impl;

/*
 * Regression tests for CWE-789 (Memory Allocation with Excessive Size Value)
 * in AESEncoder. Verifies that oversized saltLen, excessive iterations, and
 * out-of-range keyLength values read from untrusted encoded data are rejected
 * before any memory or CPU allocation occurs.
 */

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.identity4j.util.crypt.EncoderException;

public class AESEncoderCWE789Test {

    private static final AESEncoder ENCODER = new AESEncoder();
    private static final byte[] TEST_KEY;
    static {
        TEST_KEY = new byte[16];
        new java.security.SecureRandom().nextBytes(TEST_KEY);
    }

    /** Build a format-v2 payload with fully controllable header fields. */
    private static byte[] craft(int keyLength, int iterations, int saltLen, int saltBytes,
                                int trailingAfterSalt) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(keyLength);
        dos.writeShort(2);              // format version 2
        dos.writeInt(iterations);
        dos.writeShort(saltLen);
        for (int i = 0; i < saltBytes; i++)
            dos.writeByte(0xAA);
        for (int i = 0; i < trailingAfterSalt; i++)
            dos.writeByte(0xBB);
        return baos.toByteArray();
    }

    // ---- decode() ----

    @Test(expected = EncoderException.class)
    public void decode_excessiveSaltLen_throwsEncoderException() throws Exception {
        // saltLen = 257, exceeds MAX_SALT_LEN (256)
        byte[] payload = craft(128, 1024, 257, 257, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_maxShortSaltLen_throwsEncoderException() throws Exception {
        // saltLen = Short.MAX_VALUE (32767)
        byte[] payload = craft(128, 1024, Short.MAX_VALUE, 0, 0);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_excessiveIterations_throwsEncoderException() throws Exception {
        // iterations = 2_000_000, exceeds MAX_ITERATIONS (1_000_000)
        byte[] payload = craft(128, 2_000_000, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_maxIntIterations_throwsEncoderException() throws Exception {
        // iterations = Integer.MAX_VALUE
        byte[] payload = craft(128, Integer.MAX_VALUE, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_zeroIterations_throwsEncoderException() throws Exception {
        byte[] payload = craft(128, 0, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_negativeIterations_throwsEncoderException() throws Exception {
        byte[] payload = craft(128, -1, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_excessiveKeyLength_throwsEncoderException() throws Exception {
        // keyLength = 32767 bits
        byte[] payload = craft(Short.MAX_VALUE, 1024, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_zeroKeyLength_throwsEncoderException() throws Exception {
        byte[] payload = craft(0, 1024, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_negativeKeyLength_throwsEncoderException() throws Exception {
        byte[] payload = craft(-1, 1024, 16, 16, 64);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    // ---- match() ----

    @Test
    public void match_excessiveSaltLen_returnsFalse() throws Exception {
        byte[] payload = craft(128, 1024, 257, 257, 64);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_maxShortSaltLen_returnsFalse() throws Exception {
        byte[] payload = craft(128, 1024, Short.MAX_VALUE, 0, 0);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_excessiveIterations_returnsFalse() throws Exception {
        byte[] payload = craft(128, 2_000_000, 16, 16, 64);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_maxIntIterations_returnsFalse() throws Exception {
        byte[] payload = craft(128, Integer.MAX_VALUE, 16, 16, 64);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_zeroIterations_returnsFalse() throws Exception {
        byte[] payload = craft(128, 0, 16, 16, 64);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_excessiveKeyLength_returnsFalse() throws Exception {
        byte[] payload = craft(Short.MAX_VALUE, 1024, 16, 16, 64);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_zeroKeyLength_returnsFalse() throws Exception {
        byte[] payload = craft(0, 1024, 16, 16, 64);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }
}
