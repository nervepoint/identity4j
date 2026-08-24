package com.identity4j.util.crypt.impl;

/*
 * Regression tests for CWE-130 (Improper Handling of Length Parameter
 * Inconsistency) in PBEWithMD5AndDESEncoder. Verifies that:
 * (a) a payload whose first byte has the high bit set (signed negative,
 *     unsigned ≥ 128) is handled via unsigned conversion (& 0xFF), and
 * (b) a saltLen that exceeds the remaining payload is rejected cleanly.
 */

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import com.identity4j.util.crypt.EncoderException;

public class PBEWithMD5AndDESEncoderCWE130Test {

    private static final PBEWithMD5AndDESEncoder ENCODER = new PBEWithMD5AndDESEncoder();
    private static final byte[] PASSPHRASE = "testpassword".getBytes();

    /** Craft a payload: first byte = saltLenByte, rest is filler. */
    private static byte[] craftPayload(byte saltLenByte, int totalLength) {
        byte[] data = new byte[totalLength];
        data[0] = saltLenByte;
        return data;
    }

    // --- decode() ---

    @Test(expected = EncoderException.class)
    public void decode_highBitSaltLenByte_tooLargeForPayload_throwsEncoderException() throws Exception {
        // First byte 0x80 (-128 signed, 128 unsigned). Payload has only 10 bytes total,
        // so saltLen=128 > 9 (totalLength-1) → must throw, not NegativeArraySizeException.
        byte[] payload = craftPayload((byte) 0x80, 10);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_0xFF_saltLenByte_throwsEncoderException() throws Exception {
        // First byte 0xFF (-1 signed, 255 unsigned). Only 5 bytes total → 255 > 4.
        byte[] payload = craftPayload((byte) 0xFF, 5);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }

    @Test(expected = EncoderException.class)
    public void decode_saltLenEqualsTotalLength_throwsEncoderException() throws Exception {
        // saltLen byte = 10, total payload = 11 bytes (1 + 10), zero-length crypt data.
        // Then saltLen = 10 == totalLength - 1 = 10 → OK for salt, crypted.length = 0.
        // That is a degenerate but valid case, so test saltLen > remaining instead.
        // saltLen byte = 11, total payload = 11 → 11 > 10 → must throw.
        byte[] payload = craftPayload((byte) 11, 11);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }

    // --- roundtrip: valid encode/decode must still work after the fix ---

    @Test
    public void validRoundtrip_encodeDecodePreservesPlaintext() throws Exception {
        byte[] plaintext = "hello world".getBytes("UTF-8");
        byte[] encoded = ENCODER.encode(plaintext, null, PASSPHRASE, "UTF-8");
        byte[] decoded = ENCODER.decode(encoded, null, PASSPHRASE, "UTF-8");
        assertArrayEquals(plaintext, decoded);
    }

    /**
     * Verifies that the unsigned-byte fix (& 0xFF) means a high-bit salt-length
     * byte is interpreted as a large positive value and caught by the bounds
     * check, not silently converted to a negative array size.
     * Before the fix: NegativeArraySizeException (unchecked).
     * After the fix:  EncoderException("invalid salt length").
     */
    @Test(expected = EncoderException.class)
    public void decode_highBitSaltLenByte_treatedAsUnsignedAndRejected_notNegativeArraySize() throws Exception {
        // 0xFF → unsigned 255 > payload.length-1 (9) → EncoderException
        byte[] payload = craftPayload((byte) 0xFF, 10);
        ENCODER.decode(payload, null, PASSPHRASE, "UTF-8");
    }
}
