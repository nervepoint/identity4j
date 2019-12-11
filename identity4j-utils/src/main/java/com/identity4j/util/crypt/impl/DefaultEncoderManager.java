package com.identity4j.util.crypt.impl;

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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.identity4j.util.crypt.EncoderManager;
import com.identity4j.util.crypt.nss.DefaultNssTokenDatabase;
import com.identity4j.util.crypt.nss.FIPSEncoder;
import com.identity4j.util.crypt.nss.NSSEncoder;
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
                NssTokenDatabase ntdp = DefaultNssTokenDatabase.getInstance();
                addEncoder(new NSSEncoder(ntdp)); 
                addEncoder(new Base64NSSEncoder(ntdp));
                addEncoder(new FIPSEncoder(ntdp)); 
                addEncoder(new Base64FIPSEncoder(ntdp));
            }
            catch(IOException e) {
                log.info("Failed to initialize Nss. No NSS encoders registered.", e);
            }
            catch(Throwable e) {
                log.warn("Failed to initialize FIPS encoder.", e);
            }
            
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static EncoderManager getInstance() {
		return INSTANCE;
	}

	public static void main(String[] args) throws Exception {
		DefaultNssTokenDatabase toks = new DefaultNssTokenDatabase();
		toks.start();
	}
}
