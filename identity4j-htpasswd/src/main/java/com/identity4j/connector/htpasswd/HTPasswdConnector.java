package com.identity4j.connector.htpasswd;

/*
 * #%L
 * Identity4J HTPasswd
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

import com.identity4j.connector.flatfile.AbstractFlatFileConnector;
import com.identity4j.connector.flatfile.FlatFileConfiguration;
import com.identity4j.connector.unix.UnixConnector;
import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;
import com.identity4j.util.crypt.impl.PlainEncoder;
import com.identity4j.util.crypt.impl.SHAStringEncoder;
import com.identity4j.util.crypt.impl.UnixDESEncoder;

public class HTPasswdConnector extends AbstractFlatFileConnector<FlatFileConfiguration> {
    
    static {
        // Load the UNIX encoders
        try {
            Class.forName(UnixConnector.class.getName());
            DefaultEncoderManager.getInstance().addEncoder(new HTPasswdMD5Encoder());
        }
        catch(ClassNotFoundException cnfe) {            
        }
    }
    
    public HTPasswdConnector() {
        super(SHAStringEncoder.ID, UnixDESEncoder.ID, HTPasswdMD5Encoder.ID, PlainEncoder.ID);
    }

    protected int getColumnCount() {
        return 2;
    }
    
    @Override
    protected Encoder getEncoderForStoredPassword(char[] storedPassword) throws UnsupportedEncodingException {
        Encoder encoder = super.getEncoderForStoredPassword(storedPassword);
        if(encoder == null) {
            return getEncoderManager().getEncoderById(UnixDESEncoder.ID);
        }
        return encoder;
    }
}