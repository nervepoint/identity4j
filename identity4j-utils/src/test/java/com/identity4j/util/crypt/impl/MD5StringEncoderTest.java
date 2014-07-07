package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.MD5StringEncoder;

public class MD5StringEncoderTest extends AbstractEncoderTest {

    public MD5StringEncoderTest() throws UnsupportedEncodingException {
        super(MD5StringEncoder.ID, new byte[][] { "{MD5}ygWSaZhptnPLKB5JZjcklA==".getBytes("UTF-8"),
                        "{MD5}NMcUwR8UQe0uAYMWNoncGA==".getBytes("UTF-8"),
                        "{MD5}L4buR3cNpRjbR/zuCM1rCw==".getBytes("UTF-8") }, null, null, false, true);
    }

}
