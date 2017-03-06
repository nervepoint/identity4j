package com.identity4j.util.crypt.nss;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class DefaultNssTokenDatabase extends NssTokenDatabase {

    private static NssTokenDatabase instance;

    public static NssTokenDatabase getInstance() throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
                    IOException, InterruptedException {
        if (instance == null)
            throw new IOException(
                            "State NssTokenDatabase not initialized, please construct an instance once to register an instance.");
        return instance;
    }

    {
        instance = this;
    }

    public DefaultNssTokenDatabase() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
        InterruptedException {
        super();
    }

    public DefaultNssTokenDatabase(byte[] noise, byte[] passphrase) {
        super(noise, passphrase);
    }

    public DefaultNssTokenDatabase(File dbDir, byte[] noise, byte[] passphrase) {
        super(dbDir, noise, passphrase);
    }

    public DefaultNssTokenDatabase(File dbDir) {
        super(dbDir);
    }

}
