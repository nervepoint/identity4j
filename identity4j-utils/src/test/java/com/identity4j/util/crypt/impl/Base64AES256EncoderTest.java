package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AES256EncoderTest extends AbstractEncoderTest {

    public Base64AES256EncoderTest() throws UnsupportedEncodingException {
        super(Base64AES256Encoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AQAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAACkPb/70IlxmVFGTSVRf0ru".getBytes("UTF-8"),
                        "AQAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAABGDuQekGpvwYuEDFpdFo5juU6O8Pd1OtVp8894THBdYw==".getBytes("UTF-8"),
                        "AQAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAAANMzrz7qeHdM7YV2fowUV/tO8qm9hzdxhR1xPjf+qlerJDSlyU6PGc2Lg8gyVtnQo=".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
