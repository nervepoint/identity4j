package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.SHABase64Encoder;

public class SHABase64EncoderTest extends AbstractEncoderTest {

    public SHABase64EncoderTest() throws UnsupportedEncodingException {
        super(SHABase64Encoder.ID, new byte[][] { "rDszI4Mgv2OXvvUWJukxE9AJuGA=".getBytes("UTF-8"),
                        "RLxtGXHPx8qFzPH6Az3QzUI5WKU=".getBytes("UTF-8"),
                        "HvGpWakY1gTem9qtNdR1ij9H4Uw=".getBytes("UTF-8") }, null,null,  false, false);
    }

}
