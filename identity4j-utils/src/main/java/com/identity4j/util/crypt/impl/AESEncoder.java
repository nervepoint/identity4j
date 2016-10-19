package com.identity4j.util.crypt.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.identity4j.util.crypt.EncoderException;

public class AESEncoder extends RawAESEncoder {

    public final static String ID = RawAESEncoder.ID + "string";

    private int iterations = 1024;

    public AESEncoder() {
        super(ID);
    }
    
    public AESEncoder(String id, int keyLength) {
        super(id, keyLength);
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        return encode(toEncode, salt, passphrase, charset, keyLength, iterations);
    }

    protected byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset, int keyLength, int iterations)
                    throws EncoderException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (salt == null) {
                salt = randomBytes(cipher.getBlockSize());
            }
            salt = checkSaltLength(salt, cipher);
            return write(keyLength, iterations, salt, super.encode(toEncode, salt, passphrase, charset, keyLength, iterations));
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    private byte[] write(int keyLength, int iterations, byte[] salt, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(keyLength);
        dos.writeShort(0); // Old 2 byte iterations count
        dos.writeInt(iterations);
        dos.writeShort(salt.length);
        dos.write(salt);
        dos.write(data);
        return baos.toByteArray();
    }

    @Override
    public byte[] decode(byte[] toDecode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        try {
            if (salt != null)
                throw new IllegalArgumentException("Salt is encoded in the data for " + getId());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ByteArrayInputStream bain = new ByteArrayInputStream(toDecode);
            DataInputStream din = new DataInputStream(bain);
            int keyLength = din.readShort();
            int offset = 6;
            int iterations = din.readShort();
            if(iterations == 0) {
                iterations = din.readInt();
                offset += 4;
            }
            int saltLen = din.readShort();
            salt = new byte[saltLen];
            din.readFully(salt);
            byte[] data = new byte[toDecode.length - offset - saltLen];
            din.readFully(data);
            SecretKey secret = getSecretKey(new String(passphrase, charset).toCharArray(), salt, keyLength, iterations);
            byte[] iv = new byte[cipher.getBlockSize()];
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    @Override
    public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) {

        try {
            ByteArrayInputStream bain = new ByteArrayInputStream(encodedData);
            DataInputStream din = new DataInputStream(bain);
            int keyLength = din.readShort();
            int iterations = din.readShort();
            int offset = 6;
            if(iterations == 0) {
                iterations = din.readInt();
                offset += 4;
            }
            int saltLen = din.readShort();
            byte[] salt = new byte[saltLen];
            din.readFully(salt);
            byte[] data = new byte[encodedData.length - offset - saltLen];
            din.readFully(data);
            byte[] newEncoded = super.encode(unencodedData, salt, passphrase, charset, keyLength, iterations);
            return Arrays.equals(data, newEncoded);
        } catch (IOException ioe) {
            return false;
        }
    }

}
