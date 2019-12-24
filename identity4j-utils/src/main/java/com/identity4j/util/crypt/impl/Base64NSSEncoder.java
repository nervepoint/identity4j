package com.identity4j.util.crypt.impl;

import com.identity4j.util.crypt.nss.NSSEncoder;
import com.identity4j.util.crypt.nss.NssTokenDatabase;

public class Base64NSSEncoder extends CompoundEncoder {

    public final static String ID = NSSEncoder.ID + "-base64";

    public Base64NSSEncoder(NssTokenDatabase tokenDatabase) {
        this(ID, tokenDatabase);
    }
    
    protected Base64NSSEncoder(String id, NssTokenDatabase tokenDatabase) {
    	super(id);
        addEncoder(new NSSEncoder(tokenDatabase));
        addEncoder(new Base64Encoder());
    }

}
