package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.PlainEncoder;

public class PlainEncoderTest extends AbstractEncoderTest {

    public PlainEncoderTest() throws UnsupportedEncodingException {
        super(PlainEncoder.ID, new byte[][] { "asecret".getBytes("UTF-8"),
                        "a slightly longer secret".getBytes("UTF-8"),
                        "a secret with other characters like $£\"!&*(".getBytes("UTF-8") }, null, null, true, false);
    }

}
