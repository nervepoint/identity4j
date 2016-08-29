package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Base64AESEncoderTest extends AbstractEncoderTest {

    public Base64AESEncoderTest() throws UnsupportedEncodingException {
        super(Base64AESEncoder.ID, true, false);
        setExpectedHashes(new byte[][] { "AIAEAAAQAAAAAAAAAAAAAAAAAAAAANPDeZLiZid8k0RClKj5s04=".getBytes("UTF-8"),
                        "AIAEAAAQAAAAAAAAAAAAAAAAAAAAANbbIKN0h0wNA1kR+8Pa/wUw47CYjxfoaIH6wdlJqMPf".getBytes("UTF-8"),
                        "AIAEAAAQAAAAAAAAAAAAAAAAAAAAAH51lauQf7MOtYe9u2FtPMQpM6ajsPhFLnyTWGVn/WOZIbnPXlU2q1HkXUgKN3DXKA==".getBytes("UTF-8") });
        setPassphrases(
            new byte[][] { "password1".getBytes("UTF-8"), "password2".getBytes("UTF-8"), "password3".getBytes("UTF-8") });
        setSalts(new byte[][] { new byte[0], new byte[0], new byte[0] });
    }
}
