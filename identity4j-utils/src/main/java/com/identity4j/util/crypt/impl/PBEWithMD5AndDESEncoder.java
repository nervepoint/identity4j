package com.identity4j.util.crypt.impl;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.identity4j.util.crypt.EncoderException;

public class PBEWithMD5AndDESEncoder extends AbstractEncoder {
	/**
	 * Iteration count used in combination with the salt to create the
	 * encryption key.
	 */
	private final static int COUNT = 17;

	/** Name of encryption method */
	private static final String CRYPT_METHOD = "PBEWithMD5AndDES";

	public final static String ID = "pbe-with-md5-and-des";

	/** Salt */
	private final static byte[] DEFAULT_SALT = { (byte)0x15, (byte)0x8c, (byte)0xa3, (byte)0x4a,
			(byte)0x66, (byte)0x51, (byte)0x2a, (byte)0xbc };

	public PBEWithMD5AndDESEncoder() {
		super(ID);
		if (Security.getProviders("Cipher." + CRYPT_METHOD).length > 0) {
			return; // we are good to go!
		}
		try {
			// Initialize and add a security provider required for encryption
			final Class<?> clazz = getClass().getClassLoader().loadClass("com.sun.crypto.provider.SunJCE");
			Security.addProvider((Provider) clazz.newInstance());
		} catch (Exception ex) {
			throw new RuntimeException("Unable to load SunJCE service provider");
		}
	}

	@Override
	public byte[] encode(byte[] toEncode, byte[] salt, byte[] passphrase, String charset) throws EncoderException {
		try {
			Crypt c = new Crypt(new String(passphrase, "UTF-8").toCharArray(), salt);
			byte[] crypted = c.encrypt(toEncode);
			byte[] finalData = new byte[c.salt.length + crypted.length + 1];
			finalData[0] = (byte) c.salt.length;
			System.arraycopy(c.salt, 0, finalData, 1, c.salt.length);
			System.arraycopy(crypted, 0, finalData, c.salt.length + 1, crypted.length);
			return finalData;
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}

	@Override
	public byte[] decode(byte[] toDecode, byte[] passphrase, String charset) throws EncoderException {
		try {
			int saltLen = toDecode[0];
			byte[] salt = new byte[saltLen];
			byte[] crypted = new byte[toDecode.length - 1 - saltLen];
			System.arraycopy(toDecode, 1, salt, 0, saltLen);
			System.arraycopy(toDecode, 1 + saltLen, crypted, 0, crypted.length);
			return new Crypt(new String(passphrase, "UTF-8").toCharArray(), salt).decrypt(crypted);
		} catch (Exception e) {
			throw new EncoderException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		Crypt c = new Crypt("A passphrase".toCharArray(), null);

		String result = "This is the result!";
		byte[] bytes = result.getBytes();
		byte[] enc = c.encrypt(bytes);

		c = new Crypt("A passphrase".toCharArray(), null);
		byte[] dec = c.decrypt(enc);

		if (Arrays.equals(bytes, dec)) {
			System.out.println("OK!");
		} else {
			System.out.println("Different");
		}

	}

	public static class Crypt {

		private byte[] salt;
		private SecretKey key;

		public Crypt(char[] pass, byte[] salt) throws SecurityException {
			if (salt == null) {
				salt = DEFAULT_SALT;
			}
			init(pass, salt, COUNT);
		}

		public void init(char[] pass, byte[] salt, int iterations) throws SecurityException {
			final PBEKeySpec spec = new PBEKeySpec(pass);
			try {
				this.key = SecretKeyFactory.getInstance(CRYPT_METHOD).generateSecret(spec);
			} catch (Exception e) {
				throw new SecurityException(e);
			}
			this.salt = salt;
		}

		public synchronized byte[] encrypt(byte[] toEncrypt) throws SecurityException {
			try {
				PBEParameterSpec spec = new PBEParameterSpec(salt, COUNT);
				Cipher ciph = Cipher.getInstance(CRYPT_METHOD);
				ciph.init(Cipher.ENCRYPT_MODE, key, spec);
				return ciph.doFinal(toEncrypt);
			} catch (Exception e) {
				throw new SecurityException("Could not encrypt: " + e.getMessage());
			}
		}

		public synchronized byte[] decrypt(byte[] toDecrypt) throws SecurityException {
			try {
				PBEParameterSpec spec = new PBEParameterSpec(salt, COUNT);
				Cipher ciph = Cipher.getInstance(CRYPT_METHOD);
				ciph.init(Cipher.DECRYPT_MODE, key, spec);
				return ciph.doFinal(toDecrypt);
			} catch (Exception e) {
				throw new SecurityException("Could not encrypt: " + e.getMessage());
			}

		}
	}

}
