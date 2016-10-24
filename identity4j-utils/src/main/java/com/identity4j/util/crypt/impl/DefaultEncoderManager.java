package com.identity4j.util.crypt.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.util.crypt.EncoderManager;
import com.identity4j.util.crypt.nss.FIPSEncoder;
import com.identity4j.util.crypt.nss.NssTokenDatabase;

public class DefaultEncoderManager extends EncoderManager {

    static Log log = LogFactory.getLog(DefaultEncoderManager.class);

	private final static EncoderManager INSTANCE = new DefaultEncoderManager();

	private DefaultEncoderManager() {
		try {
			addEncoder(new PlainEncoder());
			addEncoder(new Base64Encoder());
			addEncoder(new UnicodeEncoder());
			addEncoder(new MD5Encoder());
			addEncoder(new SHAEncoder());
			addEncoder(new MD5Base64Encoder());
			addEncoder(new SHABase64Encoder());
			addEncoder(new MD5StringEncoder());
			addEncoder(new SHAStringEncoder());
            addEncoder(new RawAESEncoder());
            addEncoder(new AESEncoder());
            addEncoder(new AES192Encoder());
            addEncoder(new AES256Encoder());
            addEncoder(new Base64AESEncoder());
            addEncoder(new Base64AES192Encoder());
            addEncoder(new Base64AES256Encoder());
			addEncoder(new PBEWithMD5AndDESEncoder());
			addEncoder(new Base64PBEWithMD5AndDESEncoder());
            addEncoder(new Drupal7Encoder());
            addEncoder(new UnixBlowfishEncoder());
            addEncoder(new UnixDESEncoder());
            addEncoder(new UnixMD5Encoder());
            addEncoder(new UnixSHA256Encoder());
            addEncoder(new UnixSHA512Encoder());
            
            try {
                NssTokenDatabase ntdp = NssTokenDatabase.getInstance();
                addEncoder(new FIPSEncoder(ntdp));
                addEncoder(new Base64FIPSEncoder(ntdp));
            }
            catch(IOException e) {
                log.info("Failed to initialize Nss. No FIPS encoders registered.");
            }
            catch(Exception e) {
                log.warn("Failed to initialize FIPS encoder.", e);
            }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static EncoderManager getInstance() {
		return INSTANCE;
	}

}
