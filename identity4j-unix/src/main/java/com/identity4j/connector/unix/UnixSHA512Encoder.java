package com.identity4j.connector.unix;

import java.io.UnsupportedEncodingException;

public class UnixSHA512Encoder extends AbstractUnixSHAEncoder {

    public static final String ID = "unix-sha512";

    public UnixSHA512Encoder() {
        super(ID, "$6$", 512);
    }
    protected String doCrypt(byte[] unencodedData, String charset, String salt) throws UnsupportedEncodingException {
        return Sha512Crypt.Sha512_crypt(new String(unencodedData, charset), salt, 0);
    }
    
}
