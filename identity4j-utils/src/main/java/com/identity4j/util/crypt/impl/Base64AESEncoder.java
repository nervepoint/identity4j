package com.identity4j.util.crypt.impl;

public class Base64AESEncoder extends CompoundEncoder {

    public final static String ID = RawAESEncoder.ID + "-base64";

    public Base64AESEncoder() {
        super(ID);
        addEncoder(new AESEncoder());
        addEncoder(new Base64Encoder());
    }

}
