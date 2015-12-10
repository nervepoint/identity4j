////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2004-2006 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////

package com.identity4j.connector.unix;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Porting from the C version of Flash Authoring
 * flashfarm://depot/main/authortool/Utility/md5.cpp#4
 * 
 * The MD5 algorithm was not ported - using JDK version available since 1.3
 * 
 * @author cmurphy
 */
public class MD5Crypt {

    /*
     * Copyright (c) 1999 University of California. All rights reserved.
     * 
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions are
     * met: 1. Redistributions of source code must retain the above copyright
     * notice, this list of conditions and the following disclaimer. 2.
     * Redistributions in binary form must reproduce the above copyright notice,
     * this list of conditions and the following disclaimer in the documentation
     * and/or other materials provided with the distribution. 3. Neither the
     * name of the author nor the names of any co-contributors may be used to
     * endorse or promote products derived from this software without specific
     * prior written permission.
     * 
     * THIS SOFTWARE IS PROVIDED BY CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
     * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     * IN NO EVENT SHALL CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
     * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     * 
     * $FreeBSD: src/lib/libcrypt/misc.c,v 1.1 1999/09/20 12:45:49 markm Exp $
     */

    static char[] itoa64 = /* 0 ... 63 => ascii - 64 */
    "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private static String cryptTo64(long v, int n) {
        StringBuilder result = new StringBuilder();
        while (--n >= 0) {
            result.append(itoa64[(int) v & 0x3f]);
            v >>= 6;
        }
        return result.toString();
    }

    /*
     * --------------------------------------------------------------------------
     * -- "THE BEER-WARE LICENSE" (Revision 42): <phk@login.dknet.dk> wrote this
     * file. As long as you retain this notice you can do whatever you want with
     * this stuff. If we meet some day, and you think this stuff is worth it,
     * you can buy me a beer in return. Poul-Henning Kamp
     * ------------------------
     * ----------------------------------------------------
     * 
     * $FreeBSD: src/lib/libcrypt/crypt-md5.c,v 1.5 1999/12/17 20:21:45 peter
     * Exp $
     */
    private static String DEFAULT_MAGIC = "$1$"; /*
                                                  * This string is magic for
                                                  * this algorithm. Having it
                                                  * this way, we can get get
                                                  * better later on
                                                  */
    private static int MD5_SIZE = 16;

    private static void memset(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    /*
     * UNIX password
     */
    public static String crypt_md5(byte[] pw, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return crypt_md5(pw, salt, DEFAULT_MAGIC);
    }

    /*
     * UNIX password
     */
    public static String crypt_md5(byte[] pw, String salt, String magic) throws NoSuchAlgorithmException,
                    UnsupportedEncodingException {
        StringBuilder passwd = new StringBuilder();
        String sp, ep;
        byte[] finalState = new byte[MD5_SIZE];

        int sl, pl, i;
        MessageDigest ctx = MessageDigest.getInstance("MD5");
        MessageDigest ctx1 = MessageDigest.getInstance("MD5");
        long l;

        /* Refine the Salt first */
        sp = salt;

        /* If it starts with the magic string, then skip that */
        if (sp.startsWith(magic)) {
            sp = sp.substring(magic.length());
        }
        byte[] saltBytes = sp.getBytes("UTF8");

        /* It stops at the first '$', max 8 chars */
        ep = sp;
        if (ep != null) {
            int end_salt = ep.indexOf('$');
            if (end_salt == -1) {
                sl = ep.length();
            } else if ((end_salt >= 0) && (end_salt <= 7)) {
                sl = end_salt + 1;
            } else {
                sl = 8;
            }
        } else {
            sl = 0;
        }

        ctx.reset();
        /* The password first, since that is what is most unknown */
        ctx.update(pw, 0, pw.length);
        /* Then our magic string */
        ctx.update(magic.getBytes("UTF8"), 0, magic.length());
        /* Then the raw salt */
        ctx.update(saltBytes, 0, sl);

        /* Then just as many characters of the MD5(pw,salt,pw) */
        ctx1.reset();
        ctx1.update(pw, 0, pw.length);
        ctx1.update(saltBytes, 0, sl);
        ctx1.update(pw, 0, pw.length);
        finalState = ctx1.digest();

        for (pl = pw.length; pl > 0; pl -= MD5_SIZE) {
            ctx.update(finalState, 0, pl > MD5_SIZE ? MD5_SIZE : pl);
        }

        /* Don't leave anything around in vm they could use. */
        memset(finalState);

        /* Then something really weird... */
        for (i = pw.length; i != 0; i >>>= 1) {
            if ((i & 1) != 0) {
                ctx.update(finalState, 0, 1);
            } else {
                ctx.update(pw, 0, 1);
            }
        }

        /* Now make the output string */
        passwd.append(magic);
        passwd.append(sp.substring(0, sl));
        passwd.append("$");

        finalState = ctx.digest();

        /*
         * and now, just to make sure things don't run too fast On a 60 Mhz
         * Pentium this takes 34 msec, so you would need 30 seconds to build a
         * 1000 entry dictionary...
         */
        for (i = 0; i < 1000; i++) {
            ctx1.reset();

            if ((i & 1) != 0) {
                ctx1.update(pw, 0, pw.length);
            } else {
                ctx1.update(finalState, 0, MD5_SIZE);
            }

            if ((i % 3) != 0) {
                ctx1.update(saltBytes, 0, sl);
            }

            if ((i % 7) != 0) {
                ctx1.update(pw, 0, pw.length);
            }

            if ((i & 1) != 0) {
                ctx1.update(finalState, 0, MD5_SIZE);
            } else {
                ctx1.update(pw, 0, pw.length);
            }

            finalState = ctx1.digest();
        }

        l = (byteToUnsigned(finalState[0]) << 16) | (byteToUnsigned(finalState[6]) << 8) | byteToUnsigned(finalState[12]);
        passwd.append(cryptTo64(l, 4));
        l = (byteToUnsigned(finalState[1]) << 16) | (byteToUnsigned(finalState[7]) << 8) | byteToUnsigned(finalState[13]);
        passwd.append(cryptTo64(l, 4));
        l = (byteToUnsigned(finalState[2]) << 16) | (byteToUnsigned(finalState[8]) << 8) | byteToUnsigned(finalState[14]);
        passwd.append(cryptTo64(l, 4));
        l = (byteToUnsigned(finalState[3]) << 16) | (byteToUnsigned(finalState[9]) << 8) | byteToUnsigned(finalState[15]);
        passwd.append(cryptTo64(l, 4));
        l = (byteToUnsigned(finalState[4]) << 16) | (byteToUnsigned(finalState[10]) << 8) | byteToUnsigned(finalState[5]);
        passwd.append(cryptTo64(l, 4));
        l = byteToUnsigned(finalState[11]);
        passwd.append(cryptTo64(l, 2));

        /* Don't leave anything around in vm they could use. */
        memset(finalState);

        return passwd.toString();
    }

    private static int byteToUnsigned(byte aByte) {
        return aByte & 0xFF;
    }

    public static boolean verifyMD5Password(String plaintext, String ciphertext) throws NoSuchAlgorithmException {
        if (ciphertext.charAt(0) != '$' || ciphertext.charAt(1) != '1' || ciphertext.charAt(2) != '$' || ciphertext.length() < 5) {
            return false;
        }

        StringBuilder salt = new StringBuilder(16);
        int idx = ciphertext.indexOf('$', 3);
        for (int i = 3; i < idx; i++) {
            salt.append(ciphertext.charAt(i));
        }

        // Encrypt the plaintext using the salt
        try {
            String our_ciphertext = crypt_md5(plaintext.getBytes("UTF8"), salt.toString());
            return our_ciphertext.equals(ciphertext);
        } catch (UnsupportedEncodingException uee) {
            // ignore
            return false;
        }
    }

    public static String md5Crypt(String password, String charset) throws NoSuchAlgorithmException {
        char saltChars[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789./".toCharArray();
        int numSaltChars = saltChars.length;

        // Generate a random salt
        StringBuilder salt = new StringBuilder(8);
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < 8; i++) {
            salt.append(saltChars[rand.nextInt(Integer.MAX_VALUE) % numSaltChars]);
        }

        try {
            if (password == null) {
                password = "";
            }
            String encrypted = crypt_md5(password.getBytes(charset), salt.toString());
            return encrypted;
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }
}
