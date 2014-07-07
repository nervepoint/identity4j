package com.identity4j.util.crypt.impl;


public class MD5StringEncoder extends MessageDigestStringEncoder {

    public final static String ID = "md5-string";

    public MD5StringEncoder() {
        super(ID, MD5Encoder.ID, "MD5");
    }
}
