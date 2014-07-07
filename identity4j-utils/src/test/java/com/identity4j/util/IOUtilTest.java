package com.identity4j.util;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.identity4j.util.IOUtil;

public class IOUtilTest {

    @Test
    public void getStringFromResource() throws IOException {
        assertEquals(IOUtil.getStringFromResource(getClass(), "res:///testresource.txt"),"test-content");
    }

    @Test
    public void getStreamFromResource() throws IOException {
        final InputStream in = IOUtil.getStreamFromResource(getClass(), "res:///testresource.txt");
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        try {
            assertEquals(r.readLine(),"test-content");
        }
        finally {
            r.close();
        }
    }
}
