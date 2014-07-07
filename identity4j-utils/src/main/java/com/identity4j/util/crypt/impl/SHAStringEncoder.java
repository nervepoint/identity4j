package com.identity4j.util.crypt.impl;


public class SHAStringEncoder extends MessageDigestStringEncoder {

    public final static String ID = "sha-string";

    public SHAStringEncoder() {
        super(ID, SHAEncoder.ID, "SHA");
    }
}
