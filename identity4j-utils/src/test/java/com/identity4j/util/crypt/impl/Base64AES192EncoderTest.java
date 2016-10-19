package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AES192EncoderTest extends AbstractEncoderTest {

    public Base64AES192EncoderTest() throws UnsupportedEncodingException {
        super(Base64AES192Encoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AMAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAAB1fkYQRDh/u7Aim67wv5Vx".getBytes("UTF-8"),
                        "AMAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAAB/xdDsVfMLYocesTZDpO2RbTnWb9pNizqNCysmvYSiCA==".getBytes("UTF-8"),
                        "AMAAAAAABAAAEAAAAAAAAAAAAAAAAAAAAADK8p2WoikARg8gDkM/NrxMGGJqds/TdCpZCHODXBY9RX8rlbgozWXBIKbaO2X3fu0=".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
