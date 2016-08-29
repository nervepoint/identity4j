package com.identity4j.util.crypt.impl;

public class AES256Encoder extends AESEncoder {

    public final static String ID = RawAESEncoder.ID + "256-string";

    public AES256Encoder() {
        super(ID, 256);
    }

}
