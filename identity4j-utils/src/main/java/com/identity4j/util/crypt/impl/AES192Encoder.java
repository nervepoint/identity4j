package com.identity4j.util.crypt.impl;

public class AES192Encoder extends AESEncoder {

    public final static String ID = RawAESEncoder.ID + "192-string";

    public AES192Encoder() {
        super(ID, 192);
    }

}
