package com.identity4j.connector.unix;

public class UnixSHA512Encoder extends AbstractUnixSHAEncoder {

    public static final String ID = "unix-sha512";

    public UnixSHA512Encoder() {
        super(ID, "$6$", 512);
    }
}
