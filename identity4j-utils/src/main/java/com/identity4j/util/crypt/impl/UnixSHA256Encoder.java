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

import com.identity4j.util.unix.Sha256Crypt;

public class UnixSHA256Encoder extends AbstractUnixSHAEncoder {

    public static final String ID = "unix-sha256";

    public UnixSHA256Encoder() {
        super(ID, "$5$", 256);
    }
    protected String doCrypt(byte[] unencodedData, String charset, String salt) throws UnsupportedEncodingException {
        String toMatch = Sha256Crypt.Sha256_crypt(new String(unencodedData, charset), salt, 0);
        return toMatch;
    }
}
