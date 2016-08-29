package com.identity4j.util.crypt.nss;

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.crypt.impl.AbstractEncoder;

public class FIPSEncoder extends AbstractEncoder {

    public final static String ID = "fips";

    private NssTokenDatabase tokenDatabase;

    public FIPSEncoder(NssTokenDatabase tokenDatabase) {
        super(ID);
        this.tokenDatabase = tokenDatabase;
    }

    @Override
    public byte[] decode(byte[] toDecode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        if (salt != null)
            throw new IllegalArgumentException("FIPS encoder does not suppport salt.");
        if (passphrase != null)
            throw new IllegalArgumentException("FIPS encoder does not suppport passphrase.");

        try {
            return tokenDatabase.decrypt(new String(toDecode, charset)).getBytes(charset);
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    @Override
    public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        if (salt != null)
            throw new IllegalArgumentException("FIPS encoder does not suppport salt.");
        if (passphrase != null)
            throw new IllegalArgumentException("FIPS encoder does not suppport passphrase.");

        try {
            return tokenDatabase.encrypt(new String(toEncode, charset)).getBytes(charset);
        } catch (Exception e) {
            throw new EncoderException(e);
        }

    }

}
