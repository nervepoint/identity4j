package com.identity4j.connector.htpasswd;

import java.io.UnsupportedEncodingException;

import com.identity4j.connector.flatfile.FlatFileConnector;
import com.identity4j.connector.unix.UnixConnector;
import com.identity4j.util.crypt.Encoder;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;
import com.identity4j.util.crypt.impl.PlainEncoder;
import com.identity4j.util.crypt.impl.SHAStringEncoder;
import com.identity4j.util.crypt.impl.UnixDESEncoder;

public class HTPasswdConnector extends FlatFileConnector {
    
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