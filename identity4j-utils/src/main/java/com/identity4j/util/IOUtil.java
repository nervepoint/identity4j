package com.identity4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

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
        InputStream in = getStreamFromResource(clazz, resourceName);
        try {
            return IOUtils.toString(in);
        }
        finally {
            IOUtils.closeQuietly(in);
        }
    }

}
