package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.unix.Sha512Crypt;

public class UnixSHA512Encoder extends AbstractUnixSHAEncoder {

    public static final String ID = "unix-sha512";

    public UnixSHA512Encoder() {
        super(ID, "$6$", 512);
    }
    protected String doCrypt(byte[] unencodedData, String charset, String salt) throws UnsupportedEncodingException {
        return Sha512Crypt.Sha512_crypt(new String(unencodedData, charset), salt, 0);
    }
    
}
