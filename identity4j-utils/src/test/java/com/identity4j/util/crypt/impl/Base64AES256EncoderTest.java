package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AES256EncoderTest extends AbstractEncoderTest {

    public Base64AES256EncoderTest() throws UnsupportedEncodingException {
        super(Base64AES256Encoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AQAEAAAQAAAAAAAAAAAAAAAAAAAAAKQ9v/vQiXGZUUZNJVF/Su4=".getBytes("UTF-8"),
                        "AQAEAAAQAAAAAAAAAAAAAAAAAAAAAEYO5B6Qam/Bi4QMWl0WjmO5To7w93U61Wnzz3hMcF1j".getBytes("UTF-8"),
                        "AQAEAAAQAAAAAAAAAAAAAAAAAAAAAA0zOvPup4d0zthXZ+jBRX+07yqb2HN3GFHXE+N/6qV6skNKXJTo8ZzYuDyDJW2dCg==".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
