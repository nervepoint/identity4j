package com.identity4j.util.crypt.nss;

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

import com.identity4j.util.crypt.EncoderException;
import com.identity4j.util.crypt.impl.AbstractEncoder;

public class NSSEncoder extends AbstractEncoder {

    public final static String ID = "nss";

    private NssTokenDatabase tokenDatabase;

    public NSSEncoder(NssTokenDatabase tokenDatabase) {
        this(ID, tokenDatabase);
    }
    
    protected NSSEncoder(String id, NssTokenDatabase tokenDatabase) {
        super(id);
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
