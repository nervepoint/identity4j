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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.identity4j.util.crypt.EncoderException;

public class RawAESEncoder extends AbstractEncoder {

    private static final int DEFAULT_KEY_LENGTH = 128;
    private static final int DEFAULT_ITERATIONS = 1024;

    public final static String ID = "aes";


    protected int keyLength;

    public RawAESEncoder() {
        this(ID);
    }

    public RawAESEncoder(String id) {
        this(id, DEFAULT_KEY_LENGTH);
    }


    public RawAESEncoder(int keyLength) {
        this(ID, 256);
    }

    public RawAESEncoder(String id, int keyLength) {
        super(id);
        this.keyLength = keyLength;
    }

    protected SecretKey getSecretKey(char[] password, byte[] salt, int keyLength, int iterations)
                    throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return (secret);
    }

    @Override
    public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        return encode(toEncode, salt, passphrase, charset, keyLength, DEFAULT_ITERATIONS);
    }

    protected byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset, int keyLength, int iterations) throws EncoderException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if(salt == null)
                throw new IllegalArgumentException("Salt is required for " + getId());
            
            salt = checkSaltLength(salt, cipher);
            
            SecretKey secret = getSecretKey(new String(passphrase, charset).toCharArray(), salt, keyLength, iterations);
            byte[] iv = new byte[cipher.getBlockSize()];
            cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));
            byte[] ciphertext = cipher.doFinal(toEncode);
            return (ciphertext);
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

    protected byte[] checkSaltLength(byte[] salt, Cipher cipher) {
        if(salt.length == 0)
            salt = new byte[cipher.getBlockSize()];
        return salt;
    }

    @Override
    public byte[] decode(byte[] toDecode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        try {
            if(salt == null)
                throw new IllegalArgumentException("Salt is required for " + getId());
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            salt = checkSaltLength(salt, cipher);
            
            SecretKey secret = getSecretKey(new String(passphrase, charset).toCharArray(), salt, keyLength, DEFAULT_ITERATIONS);
            byte[] iv = new byte[cipher.getBlockSize()];
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
            return cipher.doFinal(toDecode);
        } catch (Exception e) {
            throw new EncoderException(e);
        }
    }

}
