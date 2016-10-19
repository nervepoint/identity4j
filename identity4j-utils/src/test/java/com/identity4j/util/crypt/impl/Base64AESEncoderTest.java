package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AESEncoderTest extends AbstractEncoderTest {

    public Base64AESEncoderTest() throws UnsupportedEncodingException {
        super(Base64AESEncoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AIAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAADTw3mS4mYnfJNEQpSo+bNO".getBytes("UTF-8"),
                        "AIAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAADW2yCjdIdMDQNZEfvD2v8FMOOwmI8X6GiB+sHZSajD3w==".getBytes("UTF-8"),
                        "AIAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAAB+dZWrkH+zDrWHvbthbTzEKTOmo7D4RS58k1hlZ/1jmSG5z15VNqtR5F1ICjdw1yg=".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
