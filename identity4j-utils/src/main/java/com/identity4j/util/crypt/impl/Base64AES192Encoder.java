package com.identity4j.util.crypt.impl;

public class Base64AES192Encoder extends CompoundEncoder {

    public final static String ID = RawAESEncoder.ID + "192-base64";

    public Base64AES192Encoder() {
        super(ID);
        addEncoder(new AES192Encoder());
        addEncoder(new Base64Encoder());
    }

}
