package com.identity4j.util.crypt.impl;


public class SHABase64Encoder extends CompoundEncoder {

    public final static String ID = "sha-base64";

    public SHABase64Encoder() {
        super(ID);
        addEncoder(new SHAEncoder());
        addEncoder(new Base64Encoder());
    }
}
