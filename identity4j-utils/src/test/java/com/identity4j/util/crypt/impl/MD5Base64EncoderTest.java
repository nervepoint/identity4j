package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.MD5Base64Encoder;

public class MD5Base64EncoderTest extends AbstractEncoderTest {

    public MD5Base64EncoderTest() throws UnsupportedEncodingException {
        super(MD5Base64Encoder.ID, new byte[][] { "ygWSaZhptnPLKB5JZjcklA==".getBytes("UTF-8"),
                        "NMcUwR8UQe0uAYMWNoncGA==".getBytes("UTF-8"),
                        "L4buR3cNpRjbR/zuCM1rCw==".getBytes("UTF-8") }, null, null, false, false);
    }

}
