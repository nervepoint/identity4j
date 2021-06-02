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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NssTokenDatabase {

	private static NssTokenDatabase instance;

	public enum Mode {
		SQL, DBM, AUTO;
	}

	public static NssTokenDatabase getInstance() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, InterruptedException {
		if (instance == null)
			throw new IllegalStateException(
					"State NssTokenDatabase not initialized, please construct an instance once to register an instance.");
		return instance;
	}

	{
		instance = this;
	}

	static Log log = LogFactory.getLog(NssTokenDatabase.class);

	private String dbPassword;
	private KeyStore keystore;
	private Provider cryptoProvider;
	private byte[] noise;
	private File dbDir;
	private File privateDir;
	private byte[] passphrase;
	private String keyName = "nam";
	private boolean fipsMode = true;
	private Mode mode = Mode.valueOf(System.getProperty("hypersocket.nss.mode", Mode.AUTO.name()));

	public NssTokenDatabase() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
			InterruptedException {
		this(new File(getNssConfigurationDirectory()));
	}

	public static String getNssConfigurationDirectory() {
		String path = System.getProperty("identity4j.nss.conf");
		if (path == null) {
			path = System.getProperty("nervepoint.conf");
		}
		if (path == null) {
			path = System.getProperty("user.home") + File.separator + ".nss";
		}
		return path;
	}

	public NssTokenDatabase(File dbDir) {
		this(dbDir, null, null);
	}

	public NssTokenDatabase(byte[] noise, byte[] passphrase) {
		this(new File(getNssConfigurationDirectory()), noise, passphrase);
	}

	public NssTokenDatabase(File dbDir, byte[] noise, byte[] passphrase) {
		this.noise = noise;
		this.passphrase = passphrase;
		this.dbDir = dbDir;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getKeyName() {
		return keyName;
	}

	public boolean isFipsMode() {
		return fipsMode;
	}

	public void setFipsMode(boolean fipsMode) {
		if (keystore != null)
			throw new IllegalStateException("FIPS mode cannot be changed if the keystore is started.");
		this.fipsMode = fipsMode;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	public Enumeration<String> keynames() throws KeyStoreException  {
		return keystore.aliases();
	}

	public void start() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		if (!dbDir.exists() && !dbDir.mkdirs()) {
			throw new IllegalStateException(String.format(
					"Could not create database directory %s.  Please ensure user %s has permissions to write to the parent directory.",
					dbDir, System.getProperty("user.name")));
		}

		this.privateDir = new File(dbDir, ".private");
		if (!privateDir.exists() && !privateDir.mkdirs()) {
			throw new IllegalStateException(String.format(
					"Could not create database private directory %s.  Please ensure user %s has permissions to write to the parent directory.",
					privateDir, System.getProperty("user.name")));
		}

		File keystoreFile = new File(privateDir, ".key");
		if (!keystoreFile.exists())
			createDatabase(keystoreFile);

		openDatabase(keystoreFile);

	}

	public String encrypt(String toEncrypt)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnrecoverableKeyException,
			KeyStoreException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		if (privateDir == null)
			throw new IllegalStateException(String.format("%s must be started.", NssTokenDatabase.class.getName()));

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cryptoProvider);
		c.init(Cipher.ENCRYPT_MODE, keystore.getKey(keyName, null));
		return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes("UTF-8")));

	}

	public String decrypt(String toDecrypt) throws InvalidKeyException, KeyStoreException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		if (privateDir == null)
			throw new IllegalStateException(String.format("%s must be started.", NssTokenDatabase.class.getName()));

		Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cryptoProvider);
		c.init(Cipher.DECRYPT_MODE, keystore.getCertificate(keyName));
		return new String(c.doFinal(Base64.decodeBase64(toDecrypt)), "UTF-8");
	}

	public static String generateString(int length) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		char[] text = new char[length];
		SecureRandom rng = new SecureRandom();
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	private void openDatabase(File keyFile)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

		/*
		 * TODO testing on this folder is .. iffy. It definitely moves around between
		 * distros, so I added this system property test as well. This seems odd to
		 * though as its a sun.* property
		 */
		File libFolder64bit = new File("/usr/lib/x86_64-linux-gnu");
		String filename = "nss.cfg";
		if ("64".equals(System.getProperty("sun.arch.data.model")) || libFolder64bit.exists()) {
			filename = "nss64.cfg";
		}
		else {
			
		}
		Mode actualMode = getActualMode();

		/*
		 * If there is no nss configuration file, generate a temporary default one
		 */
		File configFile = new File(dbDir, filename);
		if (!configFile.exists()) {
			configFile = File.createTempFile("id4j", filename);
			configFile.deleteOnExit();
			PrintWriter pw = new PrintWriter(configFile);
			try {
				if (fipsMode) {
					if (actualMode == Mode.SQL) {
						pw.println("name = NSScrypto");
//						pw.println("nssModule = keystore");
					} else {
						pw.println("name = NSSfips");
						pw.println("nssModule = fips");
					}
					pw.println("nssDbMode = readWrite");
//					pw.println("nssSecmodDirectory = " + privateDir.getAbsolutePath());
					pw.println("nssSecmodDirectory = " + (fipsMode && actualMode == Mode.SQL ? "sql:" : "") + privateDir.getAbsolutePath());
				} else {
					pw.println("name = NSScrypto");
					pw.println("nssDbMode = noDb");
				}
				pw.println("attributes = compatibility");
//				pw.println("nssLibraryDirectory = " + libFolder64bit);

				// Only OpenJDK
				if (System.getProperty("java.vm.name") != null
						&& System.getProperty("java.vm.name").indexOf("OpenJDK") != -1
						&&  System.getProperty("java.vm.name").indexOf("AdoptOpenJDK") == -1) {
					pw.println("handleStartupErrors = ignoreMultipleInitialisation");
				}

			} finally {
				pw.close();
			}
		} else {
			Properties p = new Properties();
			try (FileInputStream fin = new FileInputStream(configFile)) {
				p.load(fin);
			}
			boolean changed = false;
			if (fipsMode && !"fips".equals(p.getProperty("nssModule"))) {
				p.setProperty("nssModule", "fips");
				p.setProperty("name", "NSSfips");
				//p.setProperty("nssSecmodDirectory", (fipsMode && actualMode == Mode.SQL ? "sql:" : "") + privateDir.getAbsolutePath());
				p.setProperty("nssSecmodDirectory", privateDir.getAbsolutePath());
				p.setProperty("nssDbMode", "readWrite");
				p.remove("attributes");
				changed = true;
			} else if (!fipsMode && "fips".equals(p.getProperty("nssModule"))) {
				p.remove("nssModule");
				p.remove("nssSecmodDirectory");
				p.setProperty("name", "NSScrypto");
				p.setProperty("attributes", "compatibility");
				p.setProperty("nssDbMode", "noDb");
				changed = true;
			}
			if (changed) {
				try (FileOutputStream fos = new FileOutputStream(configFile)) {
					p.store(fos, "Identity4J NSS configuration");
				}
			}
		}

		for (Provider p : Security.getProviders()) {
			log.info(p.getName());
		}

		Class<?> clz;
		try {
			clz = Class.forName("sun.security.pkcs11.SunPKCS11");
			cryptoProvider = Security.getProvider("SunPKCS11");
			String configPath = configFile.getAbsolutePath();
			if (cryptoProvider == null) {
				try {
					Constructor<?> constructor = clz.getConstructor(String.class);
					constructor.setAccessible(true);
					cryptoProvider = (Provider) constructor.newInstance(configPath);
				} catch (InvocationTargetException | NoSuchMethodException nsme) {
					Constructor<?> constructor = clz.getConstructor();
					constructor.setAccessible(true);
					cryptoProvider = (Provider) constructor.newInstance();
					cryptoProvider = (Provider) cryptoProvider.getClass().getMethod("configure", String.class)
							.invoke(cryptoProvider, configPath);
				}
			} else {
				cryptoProvider = (Provider) cryptoProvider.getClass().getMethod("configure", String.class)
						.invoke(cryptoProvider, configPath);
			}
			try (FileInputStream fin = new FileInputStream(keyFile)) {
				dbPassword = IOUtils.toString(fin, "US-ASCII");
			}
			char[] nssDBPassword = dbPassword.toCharArray();
			keystore = KeyStore.getInstance("PKCS11", cryptoProvider);
			keystore.load(null, nssDBPassword);
		} catch (ClassNotFoundException e) {
			throw new KeyStoreException("No SunPKCS11 provider on classpath.", e);
		} catch (InstantiationException e) {
			throw new KeyStoreException("SunPKCS11 could not be instantiated.", e);
		} catch (InvocationTargetException e) {
			throw new KeyStoreException("SunPKCS11 could not be started.", e);
		} catch (NoSuchMethodException e) {
			throw new KeyStoreException("SunPKCS11 did not conform to expected API.", e);
		} catch (IllegalAccessException e) {
			throw new KeyStoreException("SunPKCS11 could not be accessed.", e);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (SecurityException e) {
			throw e;
		}

	}

	public List<String> getSecretKeyNames() throws IOException {
		try {
			List<String> keyNames = new ArrayList<>();
			for(Enumeration<String> en = keystore.aliases(); en.hasMoreElements(); ) {
				keyNames.add(en.nextElement());
			}
			return keyNames;
		} catch (KeyStoreException e) {
			throw new IOException(e.getMessage(), e);
		} 
	}

	public List<SecretKey> getSecretKeys() throws IOException {
		try {
			List<SecretKey> keys = new ArrayList<>();
			for(Enumeration<String> en = keystore.aliases(); en.hasMoreElements(); ) {
				keys.add((SecretKey) keystore.getKey(en.nextElement(), dbPassword.toCharArray()));
			}
			return keys;
		} catch (UnrecoverableKeyException e) {
			throw new IOException(e.getMessage(), e);
		} catch (KeyStoreException e) {
			throw new IOException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	public void createSecretKey(String reference) throws IOException {

		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES", cryptoProvider);
			kg.init(256);
			SecretKey skey = kg.generateKey();
			keystore.setEntry(reference, new KeyStore.SecretKeyEntry(skey),
					new KeyStore.PasswordProtection(dbPassword.toCharArray()));
			keystore.store(null, dbPassword.toCharArray());
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e.getMessage(), e);
		} catch (KeyStoreException e) {
			throw new IOException(e.getMessage(), e);
		} catch (CertificateException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	public SecretKey getSecretKey(String reference) throws IOException {
		try {
			return (SecretKey) keystore.getKey(reference, dbPassword.toCharArray());
		} catch (UnrecoverableKeyException e) {
			throw new IOException(e.getMessage(), e);
		} catch (KeyStoreException e) {
			throw new IOException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private void createDatabase(File keyFile) throws IOException, KeyStoreException {

		/* Sanity check */
		if (check("certutil") != 1) {
			throw new IllegalStateException("Could not detect certutil. Please ensure this is installed (for "
					+ "example, on Debian this would be the libnss3-tools package.");
		}
		if (check("modutil") != 1) {
			throw new IllegalStateException("Could not detect modutil. Please ensure this is installed (for "
					+ "example, on Debian this would be the libnss3-tools package.");
		}

		FileOutputStream o = new FileOutputStream(keyFile);
		try {
			if (passphrase == null) {
				String password = new BigInteger(130, new SecureRandom()).toString(32);
				o.write(password.getBytes("US-ASCII"));
			} else {
				o.write(passphrase);
			}
			o.flush();
		} finally {
			o.close();
		}

		File noiseFile = File.createTempFile("id4jnoise", ".dat");
		noiseFile.deleteOnExit();
		boolean ok = false;
		String dbPrefix = "";
		if (isSQLMode()) {
			dbPrefix = "sql:";
		} else {
			dbPrefix = "dbm:";
		} 

		try {

			FileOutputStream out = new FileOutputStream(noiseFile);
			try {
				if (noise == null) {
					File random = new File("/dev/urandom");
					FileInputStream in = new FileInputStream(random);
					try {
						for (int i = 0; i < 128; i++) {
							out.write(in.read());
						}
					} finally {
						in.close();
					}
				} else {
					out.write(noise);
				}
				out.flush();
			} finally {
				out.close();
			}

			String db = privateDir.getAbsolutePath();

			String[] createCmd = new String[] { "certutil", "-N", "-d", dbPrefix + db, "-f", db + "/.key" };
			if (exec(false, createCmd) != 0) {
				throw new IllegalStateException("Failed to create new private key for FIPS mode, check log output.");
			}

			if (fipsMode && isDBMMode()) {
				String[] makeFips = new String[] { "modutil", "-fips", "true", "-dbdir", dbPrefix + db, "-force" };
				if (exec(false, makeFips) != 0) {
					log.error("Failed to enable FIPS mode, check log output.");
					// throw new IllegalStateException("Failed to enable FIPS mode, check log
					// output.");
				}
			}

			if (isSQLMode()) {
				File secmod = new File(privateDir, "secmod.db");
				secmod.delete();
				secmod.createNewFile();
			}

			String[] certCmd = new String[] { "certutil", "-S", "-s", "CN=Access Manager", "-n", keyName, "-x", "-t",
					"CT,C,C", "-v", "120", "-m", "1234", "-d", dbPrefix + db, "-z", noiseFile.getAbsolutePath(), "-f",
					db + "/.key", "-g", "2048" };
			if (exec(false, certCmd) != 0) {
				throw new IllegalStateException("Failed to create certificate, check log output.");
			}
			exec(false, new String[] { "chmod", "400", keyFile.getAbsolutePath() });
			exec(false, new String[] { "chmod", "500", privateDir.getAbsolutePath() });
			ok = true;
		} finally {
			noiseFile.delete();
			if (!ok) {
				FileUtils.deleteDirectory(privateDir);
			}
		}

	}

	protected boolean isDBMMode() {
		return mode == Mode.DBM || mode == Mode.AUTO;
	}

	protected boolean isSQLMode() {
		return mode == Mode.SQL;
	}

	protected Mode getActualMode() {
		File secmod = new File(privateDir, "secmod.db");
		if(secmod.exists() && secmod.length() > 0) {
			return Mode.DBM;
		}
		else
			return Mode.SQL;
	}

	private int check(String... cmd) {
		try {
			return exec(true, cmd);
		} catch (IOException ioe) {
			return -1;
		}
	}

	private int exec(boolean quiet, String... cmd) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(dbDir);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		if (quiet)
			IOUtils.copy(proc.getInputStream(), new OutputStream() {
				@Override
				public void write(int b) throws IOException {
				}
			});
		else
			IOUtils.copy(proc.getInputStream(), System.out);
		try {
			int ret = proc.waitFor();
			if (ret != 0 && !quiet) {
				log.warn(String.format("%s failed with exit code %d", cmd[0], ret));
			}
			return ret;
		} catch (InterruptedException ie) {
			throw new IOException(ie);
		}
	}
}
