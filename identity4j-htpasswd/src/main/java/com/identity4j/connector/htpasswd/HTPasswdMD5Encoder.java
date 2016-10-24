package com.identity4j.connector.htpasswd;

import com.identity4j.util.crypt.impl.UnixMD5Encoder;

public class HTPasswdMD5Encoder extends UnixMD5Encoder {

    public static final String ID = "htpasswd-md5";

    public HTPasswdMD5Encoder() {
        this(ID);
    }

    public HTPasswdMD5Encoder(String id) {
        super(id, "$apr1$");
    }
}
