package com.identity4j.util.crypt.impl;

/*
 * Regression tests for CWE-130 (Improper Handling of Length Parameter
 * Inconsistency) in AESEncoder. Verifies that malformed saltLen values in
 * a crafted payload are rejected before array allocation rather than causing
 * NegativeArraySizeException or out-of-bounds access.
 * Covers both format version 0 (old, zero-IV) and format version 2 (random IV).
 */

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.identity4j.util.crypt.EncoderException;

public class AESEncoderCWE130Test {

    private static final AESEncoder ENCODER = new AESEncoder();
    private static final byte[] TEST_KEY;
    static {
        TEST_KEY = new byte[16];
        new java.security.SecureRandom().nextBytes(TEST_KEY);
    }

    /** Craft a format-version-0 payload where saltLen short is set to the given value. */
    private static byte[] craftPayload(short saltLenValue, int trailingBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(128);          // keyLength
        dos.writeShort(0);            // old iterations = 0 → triggers readInt path
        dos.writeInt(1024);           // real iterations
        dos.writeShort(saltLenValue); // injected (potentially malicious) saltLen
        for (int i = 0; i < trailingBytes; i++)
            dos.writeByte(0xAB);
        return baos.toByteArray();
    }

    /**
     * Craft a format-version-2 payload where saltLen is valid but trailing bytes
     * are too few to hold the IV (16 bytes) + ciphertext, so the explicit
     * data-length bounds check must fire.
     */
    private static byte[] craftV2Payload(short saltLenValue, int saltBytes, int trailingAfterSalt) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(128);           // keyLength
        dos.writeShort(2);             // format version 2
        dos.writeInt(1024);            // iterations
        dos.writeShort(saltLenValue);  // saltLen
        for (int i = 0; i < saltBytes; i++)
            dos.writeByte(0xCC);       // fake salt bytes
        for (int i = 0; i < trailingAfterSalt; i++)
            dos.writeByte(0xDD);       // truncated IV / no ciphertext
        return baos.toByteArray();
    }

    // --- decode() ---

    @Test(expected = EncoderException.class)
    public void decode_negativeSaltLen_throwsEncoderException() throws Exception {
        byte[] payload = craftPayload((short) -1, 16);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_minimumNegativeSaltLen_throwsEncoderException() throws Exception {
        byte[] payload = craftPayload(Short.MIN_VALUE, 16);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_saltLenExceedsDataLength_throwsEncoderException() throws Exception {
        // payload is 12 bytes; offset=10; saltLen=100 → 12-10-100 < 0
        byte[] payload = craftPayload((short) 100, 2);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    // --- match() ---

    @Test
    public void match_negativeSaltLen_returnsFalse() throws Exception {
        byte[] payload = craftPayload((short) -1, 16);
        boolean result = ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8");
        assertFalse(result);
    }

    @Test
    public void match_minimumNegativeSaltLen_returnsFalse() throws Exception {
        byte[] payload = craftPayload(Short.MIN_VALUE, 16);
        boolean result = ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8");
        assertFalse(result);
    }

    @Test
    public void match_saltLenExceedsDataLength_returnsFalse() throws Exception {
        byte[] payload = craftPayload((short) 100, 2);
        boolean result = ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8");
        assertFalse(result);
    }

    // --- roundtrip: valid encode/decode must still work after the fix ---

    @Test
    public void validRoundtrip_encodeDecodeMatch() throws Exception {
        byte[] plaintext = "hello world".getBytes("UTF-8");
        byte[] encoded = ENCODER.encode(plaintext, null, TEST_KEY, "UTF-8");
        byte[] decoded = ENCODER.decode(encoded, null, TEST_KEY, "UTF-8");
        org.junit.Assert.assertArrayEquals(plaintext, decoded);
        org.junit.Assert.assertTrue(ENCODER.match(encoded, plaintext, TEST_KEY, "UTF-8"));
    }

    // --- format version 2: IV-aware bounds checks ---

    @Test(expected = EncoderException.class)
    public void decode_v2_negativeSaltLen_throwsEncoderException() throws Exception {
        // saltLen field is -1 (signed); explicit check must catch it
        byte[] payload = craftV2Payload((short) -1, 0, 20);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_v2_saltLenExceedsPayload_throwsEncoderException() throws Exception {
        // header=10 bytes, saltLen=50 but only 5 bytes follow → must throw, not allocate
        byte[] payload = craftV2Payload((short) 50, 5, 0);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_v2_truncatedIV_throwsEncoderException() throws Exception {
        // saltLen=4, 4 salt bytes present, only 8 bytes for IV (need 16) → must throw
        byte[] payload = craftV2Payload((short) 4, 4, 8);
        ENCODER.decode(payload, null, TEST_KEY, "UTF-8");
    }

    @Test
    public void match_v2_negativeSaltLen_returnsFalse() throws Exception {
        byte[] payload = craftV2Payload((short) -1, 0, 20);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_v2_saltLenExceedsPayload_returnsFalse() throws Exception {
        byte[] payload = craftV2Payload((short) 50, 5, 0);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }

    @Test
    public void match_v2_truncatedIV_returnsFalse() throws Exception {
        byte[] payload = craftV2Payload((short) 4, 4, 8);
        assertFalse(ENCODER.match(payload, "anything".getBytes(), TEST_KEY, "UTF-8"));
    }
}
