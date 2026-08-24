package com.identity4j.util.crypt.nss;

public class FIPSEncoder extends NSSEncoder {

    public final static String ID = "fips";

    public FIPSEncoder(NssTokenDatabase tokenDatabase) {
        super(ID, tokenDatabase);
    }

}
