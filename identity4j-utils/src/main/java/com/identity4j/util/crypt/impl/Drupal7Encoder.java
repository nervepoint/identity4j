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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.identity4j.util.crypt.EncoderException;

/**
 * http://stackoverflow.com/questions/5031662/what-is-drupals-default-password-
 * encryption-method
 */
public class Drupal7Encoder extends AbstractPHPHashEncoder {

    /*
     * https://api.drupal.org/api/drupal/includes%21password.inc/constant/
     * DRUPAL_HASH_COUNT/7.x
     */
    public final static int DRUPAL_MIN_HASH_COUNT = 7;
    public final static int DRUPAL_HASH_COUNT = 15;
    public final static int DRUPAL_MAX_HASH_COUNT = 30;
    public final static int DRUPAL_HASH_LENGTH = 55;

    public final static String ID = "drupal7";

    public Drupal7Encoder() {
        super(ID);
    }

    @Override
    public boolean isOfType(byte[] encodedBytes, String charset) {
        try {
            return new String(encodedBytes, charset).startsWith("$S$");
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }

    @Override
    public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) {

        try {
            String encoded = new String(encodedData, charset);
            String encsalt = encoded.substring(0, 12);
            return Arrays.equals(encode(unencodedData, encsalt.getBytes(charset), passphrase, charset), encodedData);

        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e);
        }
    }

    @Override
    public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        try {
            return passwordCrypt("sha512", toEncode, salt == null ? generateSalt(DRUPAL_HASH_COUNT, charset) : salt, charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e);
        }
    }

    private byte[] passwordCrypt(String algo, byte[] passphrase, byte[] salt, String charset) throws UnsupportedEncodingException {
        if (passphrase.length > 512)
            throw new IllegalArgumentException("Password too long");

        String setting = new String(salt, charset);
        

        // The first 12 characters of an existing hash are its setting string.
        setting = setting.substring(0, 12);

        if (setting.charAt(0) != '$' || setting.charAt(2) != '$')
            throw new IllegalArgumentException("Invalid salt.");

        int countLog2 = passwordGetCountLog2(setting);
        if (countLog2 < DRUPAL_MIN_HASH_COUNT || countLog2 > DRUPAL_MAX_HASH_COUNT)
            throw new IllegalArgumentException("Invalid count.");

        String hashsalt = setting.substring(4, 12);
        int count = 1 << countLog2;

        String pw = new String(passphrase, charset);
        byte[] hash = hash(algo, (hashsalt + pw).getBytes(charset), true, charset);
        do {
            byte[] newhash = new byte[hash.length + passphrase.length];
            System.arraycopy(hash, 0, newhash, 0, hash.length);
            System.arraycopy(passphrase, 0, newhash, hash.length, passphrase.length);
            hash = hash(algo, newhash, true, charset);
        } while (--count > 0);

        int len = hash.length;

        String output = setting + passwordBase64Encode(hash, len);
        int expected = 12 + (int) Math.ceil((8f * len) / 6f);
        
        if (output.length() == expected)
            return (expected > DRUPAL_HASH_LENGTH ? output.substring(0, DRUPAL_HASH_LENGTH) : output).getBytes(charset);

        throw new IllegalArgumentException("Could not encode.");
    }

    private int passwordGetCountLog2(String setting) {
        return passwordItoa64().indexOf(setting.charAt(3));
    }

    private byte[] generateSalt(int count, String charset) {
        String output = "$S$";
        count = enforceLog2Boundaries(count);
        output += passwordItoa64().charAt(count);
        output += passwordBase64Encode(randomBytes(6), 6);
        try {
            return output.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String passwordBase64Encode(byte[] input, int count) {
        String output = "";
        for (int i = 0; i < count;) {
            int val = input[i++] & 0xff;
            output += passwordItoa64().charAt(val & 0x3f);
            if (i < count)
                val = val | ((input[i] & 0xff) << 8);
            output += passwordItoa64().charAt((val >>> 6) & 0x3f);
            if (i++ >= count)
                break;
            if (i < count)
                val = val | ((input[i] & 0xff) << 16);
            output += passwordItoa64().charAt((val >>> 12) & 0x3f);
            if (i++ >= count)
                break;
            output += passwordItoa64().charAt((val >>> 18) & 0x3f);
        }
        return output;
    }

    private int enforceLog2Boundaries(int count) {
        return Math.max(Math.min(count, DRUPAL_MAX_HASH_COUNT), DRUPAL_MIN_HASH_COUNT);
    }

    private String passwordItoa64() {
        return "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    }
}
