package com.identity4j.util.crypt.impl;


public class SHAEncoder extends MessageDigestEncoder {

    public final static String ID = "sha";

    public SHAEncoder() {
        super(ID, "SHA");
    }
}
