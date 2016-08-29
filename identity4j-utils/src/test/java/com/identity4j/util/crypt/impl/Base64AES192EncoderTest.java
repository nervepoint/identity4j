package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AES192EncoderTest extends AbstractEncoderTest {

    public Base64AES192EncoderTest() throws UnsupportedEncodingException {
        super(Base64AES192Encoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AMAEAAAQAAAAAAAAAAAAAAAAAAAAAHV+RhBEOH+7sCKbrvC/lXE=".getBytes("UTF-8"),
                        "AMAEAAAQAAAAAAAAAAAAAAAAAAAAAH/F0OxV8wtihx6xNkOk7ZFtOdZv2k2LOo0LKya9hKII".getBytes("UTF-8"),
                        "AMAEAAAQAAAAAAAAAAAAAAAAAAAAAMrynZaiKQBGDyAOQz82vEwYYmp2z9N0KlkIc4NcFj1FfyuVuCjNZcEgpto7Zfd+7Q==".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
