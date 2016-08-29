package com.identity4j.util.crypt.impl;

public class Base64AES256Encoder extends CompoundEncoder {

    public final static String ID = RawAESEncoder.ID + "256-base64";

    public Base64AES256Encoder() {
        super(ID);
        addEncoder(new AES256Encoder());
        addEncoder(new Base64Encoder());
    }

}
