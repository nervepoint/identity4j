package com.identity4j.util;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class IOUtil {
    
    /**
     * Get a stream given a resource name
     * 
     * @param resourceName resource name
     */

    /**
     * Get a string from a <i>Resource </i>. This can either be a URL, an absolute file name
     * or a relative file name. A special URL that has a scheme of <i>res</i> is also supported,
     * this gets the resource by name from the classloader that provided <code>clazz</code>.
     * 
     * @param clazz class to get classloader from (<code>null</code> to skip loading as classpath resource)
     * @param resourceName resource name
     * @return resource stream
     * @throws IOException 
     * @throws IOException on any error loading
     */
    public static InputStream getStreamFromResource(Class<?> clazz, String resourceName) throws IOException {
        
        // A resource name
        if(resourceName.startsWith("res://") && clazz != null) {
            URL url = clazz.getResource(resourceName.substring(6));
            if(url == null) {
                throw new FileNotFoundException("Resource " + resourceName + " not found on CLASSPATH.");
            }
            return url.openStream();
        }
        else {
            try {
                // A URL
                URL url = new URL(resourceName);
                return url.openStream(); 
            }
            catch(MalformedURLException murle) {
                File file = new File(resourceName);
                // A file
                return new FileInputStream(file);
            }
        }
    }

    /**
     * Get a string from a <i>Resource </i>. This can either be a URL, an absolute file name
     * or a relative file name. A special URL that has a scheme of <i>res</i> is also supported,
     * this gets the resource by name from the classloader that provided <code>clazz</code>.
     * 
     * @param clazz class to get classloader from (<code>null</code> to skip loading as classpath resource)
     * @param resourceName resource name
     * @return content of resource
     * @throws IOException 
     * @throws IOException on any error loading
     */
    public static String getStringFromResource(Class<?> clazz, String resourceName) throws IOException {
        return getStringFromResource(clazz, resourceName, null);
    }

    /**
     * Get a string from a <i>Resource </i>. This can either be a URL, an absolute file name
     * or a relative file name. A special URL that has a scheme of <i>res</i> is also supported,
     * this gets the resource by name from the classloader that provided <code>clazz</code>.
     * 
     * @param clazz class to get classloader from (<code>null</code> to skip loading as classpath resource)
     * @param resourceName resource name
     * @param charset character set
     * @return content of resource
     * @throws IOException 
     * @throws IOException on any error loading
     */
    public static String getStringFromResource(Class<?> clazz, String resourceName, String charset) throws IOException {
        try(InputStream in = getStreamFromResource(clazz, resourceName)) {
            return IOUtils.toString(in, charset == null ? Charset.defaultCharset().name() : charset);
        }
    }

}
