package com.identity4j.util.crypt.impl;


public class MD5Base64Encoder extends CompoundEncoder {

    public final static String ID = "md5-base64";

    public MD5Base64Encoder() {
        super(ID);
        addEncoder(new MD5Encoder());
        addEncoder(new Base64Encoder());
    }
}
