package com.identity4j.connector.sap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;

public class Lib {

	public static String locateLibrary() {
		final String ao = getAO();
		final ClassLoader loader = Lib.class.getClassLoader();
		try {
			URL resource = loader.getResource(ao);
			if (resource == null) {
				/*
				 * The natives are not available on the classpath, either the
				 * user hasn't included the natives Jar, or this is running in a
				 * dev environment. If dev environment, try java.library.path
				 */
				for (String f : System.getProperty("java.library.path", "").split("\\" + File.pathSeparatorChar)) {
					File file = new File(new File(f), ao);
					if (file.exists())
						return file.getAbsolutePath();
				}
				throw new RuntimeException(
						"The appropriate SAP JCo library could not be found either on the classpath or anywhere in java.library.path. Please correct this.");
			} else {
				File tmpdir = new File(new File(System.getProperty("java.io.tmpdir")), Math.abs(Lib.class.getName().hashCode()) + ".tmp" );
				if(!tmpdir.exists() && !tmpdir.mkdirs())
					throw new IOException("Could not create directory " + tmpdir);
				File tf = new File(tmpdir, ao.substring(ao.lastIndexOf('/') + 1));
					
				tf.deleteOnExit();
				FileOutputStream out = new FileOutputStream(tf);
				try {
					InputStream in = resource.openStream();
					try {
						IOUtils.copy(in, out);
					} finally {
						in.close();
					}
				} finally {
					out.close();
				}
				return tf.getAbsolutePath();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String getAO() {
		// TODO other archs? If we can get hold of them
		if (SystemUtils.IS_OS_MAC_OSX && System.getProperty("os.arch").startsWith("x86_64"))
			return "macosx/x86_64/libsapjco3.dylib";
		else if (SystemUtils.IS_OS_MAC_OSX)
			return "macosx/x86/libsapjco3.dylib";
		else if (SystemUtils.IS_OS_WINDOWS && System.getProperty("os.arch").startsWith("amd64"))
			return "win64/libsapjco3.dll";
		else if (SystemUtils.IS_OS_WINDOWS)
			return "win32/libsapjco3.dll";
		else if (SystemUtils.IS_OS_LINUX && System.getProperty("os.arch").startsWith("amd64"))
			return "linux/x86_64/libsapjco3.so";
		else if (SystemUtils.IS_OS_LINUX)
			return "linux/x86/libsapjco3.so";
		else
			throw new RuntimeException("Unsupported platform");
	}

}
