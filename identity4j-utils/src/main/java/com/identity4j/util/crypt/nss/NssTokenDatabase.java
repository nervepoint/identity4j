package com.identity4j.util.crypt.nss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NssTokenDatabase {

    static Log log = LogFactory.getLog(NssTokenDatabase.class);

    private String dbPassword;
    private KeyStore keystore;
    private Provider cryptoProvider;
    private byte[] noise;
    private File dbDir;
    private File privateDir;
    private byte[] passphrase;

    private static NssTokenDatabase instance;

    public static NssTokenDatabase getInstance()
                    throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InterruptedException {
        if (instance == null)
            throw new IOException(
                            "State NssTokenDatabase not initialized, please construct an instance once to register an instance.");
        return instance;
    }

    public NssTokenDatabase()
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InterruptedException {
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

    public NssTokenDatabase(File dbDir) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        this(dbDir, null, null);
    }

    public NssTokenDatabase(byte[] noise, byte[] passphrase)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        this(new File(getNssConfigurationDirectory(), ".private"), noise, passphrase);
    }

    public NssTokenDatabase(File dbDir, byte[] noise, byte[] passphrase)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        instance = this;

        this.noise = noise;
        this.passphrase = passphrase;

        this.dbDir = dbDir;
        if (!dbDir.exists() && !dbDir.mkdirs()) {
            throw new IOException(String.format(
                "Could not create database directory %s.  Please ensure user %s has permissions to write to the parent directory.",
                dbDir, System.getProperty("user.name")));
        }

        this.privateDir = new File(dbDir, ".private");
        if (!privateDir.exists() && !privateDir.mkdirs()) {
            throw new IOException(String.format(
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

        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cryptoProvider);
        c.init(Cipher.ENCRYPT_MODE, keystore.getKey("nam", null));
        return Base64.encodeBase64String(c.doFinal(toEncrypt.getBytes("UTF-8")));

    }

    public String decrypt(String toDecrypt) throws InvalidKeyException, KeyStoreException, NoSuchAlgorithmException,
                    NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {

        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", cryptoProvider);
        c.init(Cipher.DECRYPT_MODE, keystore.getCertificate("nam"));
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

    @SuppressWarnings("restriction")
    private void openDatabase(File keyFile) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        /*
         * TODO testing on this folder is .. iffy. It definitely moves around
         * between distros, so I added this system property test as well. This
         * seems odd to though as its a sun.* property
         */
        File libFolder64bit = new File("/usr/lib/x86_64-linux-gnu/");
        String filename = "nss.cfg";
        if ("64".equals(System.getProperty("sun.arch.data.model")) || libFolder64bit.exists()) {
            filename = "nss64.cfg";
        }

        /*
         * If there is no nss configuration file, generate a temporary default
         * one
         */
        File configFile = new File(dbDir, filename);
        if (!configFile.exists()) {
            configFile = File.createTempFile("id4j", filename);
            configFile.deleteOnExit();
            PrintWriter pw = new PrintWriter(configFile);
            try {
                pw.println("name = NSScrypto");
                pw.println("attributes = compatibility");
                // pw.println("nssLibraryDirectory =
                // /usr/lib/x86_64-linux-gnu");
                pw.println("nssSecmodDirectory = " + privateDir.getAbsolutePath());
                pw.println("nssDbMode = readWrite");
                pw.println("nssModule = fips");
            } finally {
                pw.close();
            }
        }

        for (Provider p : Security.getProviders()) {
            log.info(p.getName());
        }

        cryptoProvider = new sun.security.pkcs11.SunPKCS11(configFile.getAbsolutePath());
        dbPassword = IOUtils.toString(new FileInputStream(keyFile), "US-ASCII");
        char[] nssDBPassword = dbPassword.toCharArray();
        keystore = KeyStore.getInstance("PKCS11", cryptoProvider);
        keystore.load(null, nssDBPassword);

    }

    private void createDatabase(File keyFile) throws IOException {

        /* Sanity check */
        if (check("certutil") != 1) {
            throw new IOException("Could not detect certutil. Please ensure this is installed (for "
                            + "example, on Debian this would be the libnss3-tools package.");
        }
        if (check("modutil") != 1) {
            throw new IOException("Could not detect modutil. Please ensure this is installed (for "
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

            String[] createCmd = new String[] { "certutil", "-N", "-d", db, "-f", db + "/.key" };
            exec(false, createCmd);
            String[] makeFips = new String[] { "modutil", "-fips", "true", "-dbdir", db, "-force" };
            exec(false, makeFips);
            String[] certCmd = new String[] { "certutil", "-S", "-s", "CN=Access Manager", "-n", "nam", "-x", "-t", "CT,C,C", "-v",
                            "120", "-m", "1234", "-d", db, "-z", noiseFile.getAbsolutePath(), "-f", db + "/.key", "-g", "2048" };
            exec(false, certCmd);
            exec(false, new String[] { "chmod", "400", keyFile.getAbsolutePath() });
            exec(false, new String[] { "chmod", "500", privateDir.getAbsolutePath() });
        } finally {
            noiseFile.delete();
        }

    }

    private int check(String... cmd) {
        try {
            return exec(true, cmd);
        } catch (IOException ioe) {
            return -1;
        }
    }

    public int exec(boolean quiet, String... cmd) throws IOException {
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
