package com.identity4j.util.crypt.impl;


public class Base64PBEWithMD5AndDESEncoder extends CompoundEncoder {

    public final static String ID = PBEWithMD5AndDESEncoder.ID + "-base64";

    public Base64PBEWithMD5AndDESEncoder() {
        super(ID);
        addEncoder(new PBEWithMD5AndDESEncoder());
        addEncoder(new Base64Encoder());
    }
}
