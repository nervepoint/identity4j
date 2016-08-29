package com.identity4j.util.crypt.impl;

import com.identity4j.util.crypt.nss.FIPSEncoder;
import com.identity4j.util.crypt.nss.NssTokenDatabase;

public class Base64FIPSEncoder extends CompoundEncoder {

    public final static String ID = FIPSEncoder.ID + "-base64";

    public Base64FIPSEncoder(NssTokenDatabase tokenDatabase) {
        super(ID);
        addEncoder(new FIPSEncoder(tokenDatabase));
        addEncoder(new Base64Encoder());
    }

}
