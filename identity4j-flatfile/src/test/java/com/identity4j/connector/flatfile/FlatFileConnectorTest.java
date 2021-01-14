/* HEADER */
package com.identity4j.connector.flatfile;

/*
 * #%L
 * Identity4J Flat File
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
import com.identity4j.connector.ConnectorConfigurationParameters;

public class FlatFileConnectorTest<C extends ConnectorConfigurationParameters> extends AbstractConnectorTest<C> {

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
            try(InputStream in = obj.getContent().getInputStream()) {
                final File createTempFile = File.createTempFile("test", ".txt");
                createTempFile.deleteOnExit();
                try(FileOutputStream fos = new FileOutputStream(createTempFile)) {
                    IOUtils.copy(in, fos);
                }
                properties.put(key, createTempFile.getAbsolutePath());
            }
        } catch (IOException ioe) {
            throw new Error(ioe);
        }
    }
}
