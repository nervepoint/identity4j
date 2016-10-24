package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.unix.Sha256Crypt;

public class UnixSHA256Encoder extends AbstractUnixSHAEncoder {

    public static final String ID = "unix-sha256";

    public UnixSHA256Encoder() {
        super(ID, "$5$", 256);
    }
    protected String doCrypt(byte[] unencodedData, String charset, String salt) throws UnsupportedEncodingException {
        String toMatch = Sha256Crypt.Sha256_crypt(new String(unencodedData, charset), salt, 0);
        return toMatch;
    }
}
