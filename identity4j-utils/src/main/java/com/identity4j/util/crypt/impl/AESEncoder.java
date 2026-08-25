package com.identity4j.util.crypt.impl;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
            // CWE-665: use random IV per encryption; format version 2 stores IV after salt
            byte[] iv = randomBytes(cipher.getBlockSize());
            byte[] ciphertext = encodeWithIV(toEncode, salt, passphrase, charset, keyLength, iterations, iv);
            return writeWithIV(keyLength, iterations, salt, iv, ciphertext);
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    private byte[] writeWithIV(int keyLength, int iterations, byte[] salt, byte[] iv, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(keyLength);
        dos.writeShort(2); // format version 2: random IV stored after salt
        dos.writeInt(iterations);
        dos.writeShort(salt.length);
        dos.write(salt);
        dos.write(iv);
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
            int formatVersion = din.readShort();
            int iterations;
            if (formatVersion == 0) {
                // old format: zero IV, read 4-byte actual iterations
                iterations = din.readInt();
                offset += 4;
            } else if (formatVersion == 2) {
                // new format: random IV stored after salt
                iterations = din.readInt();
                offset += 4;
            } else {
                throw new EncoderException("Unsupported AES encoding format version: " + formatVersion);
            }
            int saltLen = din.readShort();
            if (saltLen < 0 || toDecode.length - offset - saltLen < 0)
                throw new EncoderException("Malformed encoded data: invalid salt length");
            salt = new byte[saltLen];
            din.readFully(salt);
            byte[] iv;
            if (formatVersion == 2) {
                iv = new byte[cipher.getBlockSize()];
                din.readFully(iv);
                offset += cipher.getBlockSize();
            } else {
                iv = new byte[cipher.getBlockSize()]; // zero IV (backward compat)
            }
            int dataLen = toDecode.length - offset - saltLen;
            if (dataLen < 0)
                throw new EncoderException("Malformed encoded data: payload too short for ciphertext");
            byte[] data = new byte[dataLen];
            din.readFully(data);
            SecretKey secret = getSecretKey(new String(passphrase, charset).toCharArray(), salt, keyLength, iterations);
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
            int formatVersion = din.readShort();
            int offset = 6;
            int iterations;
            if (formatVersion == 0) {
                iterations = din.readInt();
                offset += 4;
            } else if (formatVersion == 2) {
                iterations = din.readInt();
                offset += 4;
            } else {
                return false;
            }
            int saltLen = din.readShort();
            if (saltLen < 0 || encodedData.length - offset - saltLen < 0)
                return false;
            byte[] salt = new byte[saltLen];
            din.readFully(salt);
            if (formatVersion == 2) {
                // read stored IV; re-encrypt plaintext with same salt+IV and compare ciphertexts
                int blockSize = Cipher.getInstance("AES/CBC/PKCS5Padding").getBlockSize();
                byte[] iv = new byte[blockSize];
                din.readFully(iv);
                offset += blockSize;
                int dataLen = encodedData.length - offset - saltLen;
                if (dataLen < 0)
                    return false;
                byte[] data = new byte[dataLen];
                din.readFully(data);
                byte[] reEncoded = encodeWithIV(unencodedData, salt, passphrase, charset, keyLength, iterations, iv);
                return Arrays.equals(data, reEncoded);
            } else {
                byte[] data = new byte[encodedData.length - offset - saltLen];
                din.readFully(data);
                byte[] newEncoded = super.encode(unencodedData, salt, passphrase, charset, keyLength, iterations);
                return Arrays.equals(data, newEncoded);
            }
        } catch (Exception e) {
            return false;
        }
    }
}
