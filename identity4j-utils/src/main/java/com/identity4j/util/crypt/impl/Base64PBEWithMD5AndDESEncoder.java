package com.identity4j.util.crypt.impl;


public class Base64PBEWithMD5AndDESEncoder extends CompoundEncoder {

    public final static String ID = PBEWithMD5AndDESEncoder.ID + "-base64";

    public Base64PBEWithMD5AndDESEncoder() {
        super(ID);
        addEncoder(new PBEWithMD5AndDESEncoder());
        addEncoder(new Base64Encoder());
    }
    
    public static void main(String[] args) {
        Base64PBEWithMD5AndDESEncoder d = new Base64PBEWithMD5AndDESEncoder();
        System.out.println(new String(d.decode(args[0].getBytes(), new StringBuilder(args[1]).reverse().toString().getBytes(), "UTF-8")));
    }
}
