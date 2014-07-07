/* HEADER */
package com.identity4j.connector.flatfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

import com.identity4j.connector.AbstractConnectorTest;
import com.identity4j.connector.flatfile.FlatFileConfiguration;

public class FlatFileConnectorTest extends AbstractConnectorTest {

    public FlatFileConnectorTest() {
        this("/flatfile-connector.properties");
    }

    protected FlatFileConnectorTest(String propertiesResourceName) {
        super(propertiesResourceName);
    }

    @Test
    public void test() {
        // Makes Eclipse think it can test this class
    }

    @Override
    protected void onLoadConfigurationProperties(Properties properties) {
        // Copy the template test flat file
        copyResourceToTemporaryFile(FlatFileConnectorTest.class, properties, FlatFileConfiguration.KEY_FILENAME, "/flatfile.txt");
    }

    protected final void copyResourceToTemporaryFile(Class<?> resourceClass, Properties properties, String key, String defaultValue)
                    throws Error {
        try {
            final String fileName = properties.getProperty(key, defaultValue);
            FileObject obj = VFS.getManager().resolveFile(fileName);
            InputStream in = obj.getContent().getInputStream();
            try {
                final File createTempFile = File.createTempFile("test", ".txt");
                createTempFile.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(createTempFile);
                try {
                    IOUtils.copy(in, fos);
                } finally {
                    IOUtils.closeQuietly(fos);
                }
                properties.put(FlatFileConfiguration.KEY_FILENAME, createTempFile.getAbsolutePath());
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (IOException ioe) {
            throw new Error(ioe);
        }
    }
}
