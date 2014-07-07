package com.identity4j.connector.unix;

public class UnixSHA256Encoder extends AbstractUnixSHAEncoder {

    public static final String ID = "unix-sha256";

    public UnixSHA256Encoder() {
        super(ID, "$5$", 256);
    }
}
