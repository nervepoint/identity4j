package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.Base64Encoder;

public class Base64EncoderTest extends AbstractEncoderTest {

    public Base64EncoderTest() throws UnsupportedEncodingException {
        super(Base64Encoder.ID, new byte[][] { "YXNlY3JldA==".getBytes("UTF-8"),
                        "YSBzbGlnaHRseSBsb25nZXIgc2VjcmV0".getBytes("UTF-8"),
                        "YSBzZWNyZXQgd2l0aCBvdGhlciBjaGFyYWN0ZXJzIGxpa2UgJMKjIiEmKig=".getBytes("UTF-8") }, null, null, true, false);
    }

}
