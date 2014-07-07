package com.identity4j.util.crypt.impl;


public class MD5Encoder extends MessageDigestEncoder {

    public final static String ID = "md5";

    public MD5Encoder() {
        super(ID, "MD5");
    }
}
