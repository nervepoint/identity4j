package com.identity4j.util.crypt.impl;

/*
 * Regression tests for CWE-130 (Improper Handling of Length Parameter
 * Inconsistency) in AESEncoder. Verifies that malformed saltLen values in
 * a crafted payload are rejected before array allocation rather than causing
 * NegativeArraySizeException or out-of-bounds access.
 */

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.identity4j.util.crypt.EncoderException;

public class AESEncoderCWE130Test {

    private static final AESEncoder ENCODER = new AESEncoder();
    private static final byte[] PASSPHRASE = "testpassword".getBytes();

    /** Craft a payload where saltLen short is set to the given value. */
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

    // --- decode() ---

    @Test(expected = EncoderException.class)
    public void decode_negativeSaltLen_throwsEncoderException() throws Exception {
        byte[] payload = craftPayload((short) -1, 16);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_minimumNegativeSaltLen_throwsEncoderException() throws Exception {
        byte[] payload = craftPayload(Short.MIN_VALUE, 16);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_saltLenExceedsDataLength_throwsEncoderException() throws Exception {
        // payload is 12 bytes; offset=10; saltLen=100 → 12-10-100 < 0
        byte[] payload = craftPayload((short) 100, 2);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }

    // --- match() ---

    @Test
    public void match_negativeSaltLen_returnsFalse() throws Exception {
        byte[] payload = craftPayload((short) -1, 16);
        boolean result = ENCODER.match(payload, "anything".getBytes(), PASSPHRASE, "UTF-8");
        assertFalse(result);
    }

    @Test
    public void match_minimumNegativeSaltLen_returnsFalse() throws Exception {
        byte[] payload = craftPayload(Short.MIN_VALUE, 16);
        boolean result = ENCODER.match(payload, "anything".getBytes(), PASSPHRASE, "UTF-8");
        assertFalse(result);
    }

    @Test
    public void match_saltLenExceedsDataLength_returnsFalse() throws Exception {
        byte[] payload = craftPayload((short) 100, 2);
        boolean result = ENCODER.match(payload, "anything".getBytes(), PASSPHRASE, "UTF-8");
        assertFalse(result);
    }

    // --- roundtrip: valid encode/decode must still work after the fix ---

    @Test
    public void validRoundtrip_encodeDecodeMatch() throws Exception {
        byte[] plaintext = "hello world".getBytes("UTF-8");
        byte[] encoded = ENCODER.encode(plaintext, null, PASSPHRASE, "UTF-8");
        byte[] decoded = ENCODER.decode(encoded, null, PASSPHRASE, "UTF-8");
        org.junit.Assert.assertArrayEquals(plaintext, decoded);
        org.junit.Assert.assertTrue(ENCODER.match(encoded, plaintext, PASSPHRASE, "UTF-8"));
    }
}
