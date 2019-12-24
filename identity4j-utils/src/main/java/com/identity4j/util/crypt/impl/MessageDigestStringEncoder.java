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

import com.identity4j.util.crypt.EncoderException;

public class MessageDigestStringEncoder extends CompoundEncoder {

    private String hashAlgorithm;

    public MessageDigestStringEncoder(String id, String encryptionEncoderId, String hashAlgorithm) {
        super(id);
        addEncoder(new MessageDigestEncoder(encryptionEncoderId, hashAlgorithm));
        addEncoder(new Base64Encoder());
        this.hashAlgorithm = hashAlgorithm;
    }

    @Override
    public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
        try {
            return ("{" + hashAlgorithm + "}" + new String(super.encode(toEncode, salt, passphrase, charset), charset))
                            .getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e);
        }
    }

    @Override
    public boolean match(byte[] encodedData, byte[] unencodedData, byte[] passphrase, String charset) {
        try {
            String hashstr = new String(encodedData, charset);
            if (hashstr.startsWith("{")) {
                int idx = hashstr.indexOf("}");
                String hashAlgo = hashstr.substring(1, idx);
                if (hashAlgo.equals(hashAlgorithm)) {
                    hashstr = hashstr.substring(idx + 1);
                    return super.match(hashstr.getBytes(charset), unencodedData, passphrase, charset);
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e);
        }
        return false;
    }

    @Override
    public boolean isOfType(byte[] encodedBytes, String charset) {
        try {
            return new String(encodedBytes, charset).startsWith("{" + hashAlgorithm + "}");
        } catch (UnsupportedEncodingException e) {
            throw new EncoderException(e);
        }
    }

}
