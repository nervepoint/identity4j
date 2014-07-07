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
 * Adapted to SHA hash by http://nervepoint.com
 * 
 * @author cmurphy
 */
public class SHACrypt {

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

    private static void memset(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    /*
     * UNIX password
     */
    public static String crypt_sha(byte[] pw, String salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return crypt_sha(pw, salt, 512);
    }

    /*
     * UNIX password
     */
    public static String crypt_sha(byte[] pw, String salt, int size) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String magic = null;
        switch (size) {
            case 256:
                magic = "$5$";
                break;
            case 512:
                magic = "$6$";
                break;
            default:
                throw new IllegalArgumentException("Size must be 256 or 512");
        }
        int blockSize = size == 512 ? 64 : 32;

        StringBuilder outputString = new StringBuilder();
        String saltText, ep;
        byte[] altResult = new byte[size == 512 ? 86 : 43];
        byte[] tempResult = new byte[size == 512 ? 86 : 43];

        int saltLength, cnt;
        MessageDigest ctx = MessageDigest.getInstance("SHA-" + size);
        MessageDigest altCtx = MessageDigest.getInstance("SHA-" + size);

        /* Refine the Salt first */
        saltText = salt;

        /* If it starts with the magic string, then skip that */
        if (saltText.startsWith(magic)) {
            saltText = saltText.substring(magic.length());
        }

        /* May specify number of rounds */
        long rounds = 5000;
        boolean customRounds = false;
        if (saltText.startsWith("rounds=")) {
            rounds = Long.parseLong(saltText.substring(7, saltText.indexOf('$')));
            if (rounds < 1000 || rounds > 999999999) {
                throw new IllegalArgumentException("Invalid number of rounds. Must be between 1000 and 999999999");
            }
            saltText = saltText.substring(saltText.indexOf('$') + 1);
            customRounds = true;
        }

        byte[] saltBytes = saltText.getBytes("UTF8");

        /* It stops at the first '$', max 16 chars */
        ep = saltText;
        if (ep != null) {
            int endSalt = ep.indexOf('$');
            if (endSalt == -1) {
                saltLength = ep.length();
            } else if ((endSalt >= 0) && (endSalt <= 15)) {
                saltLength = endSalt + 1;
            } else {
                saltLength = 16;
            }
        } else {
            saltLength = 0;
        }

        ctx.reset();
        ctx.update(pw, 0, pw.length);
        ctx.update(saltBytes, 0, saltLength);

        /*
         * Compute alternate SHA sum with input KEY, SALT, and KEY. The final
         * result will be added to the first context.
         */
        altCtx.reset();
        altCtx.update(pw, 0, pw.length);
        altCtx.update(saltBytes, 0, saltLength);
        altCtx.update(pw, 0, pw.length);
        altResult = altCtx.digest();

        /*
         * For each block of 32 or 64 bytes in the password string add digest B
         * to digest A
         */
        for (cnt = pw.length; cnt > blockSize; cnt -= blockSize) {
            ctx.update(altResult, 0, blockSize);
        }
        ctx.update(altResult, 0, cnt);

        /*
         * Take the binary representation of the length of the key and for every
         * 1 add the alternate sum, for every 0 the key.
         */
        for (cnt = pw.length; cnt > 0; cnt >>= 1) {
            if ((cnt & 1) != 0) {
                ctx.update(altResult, 0, blockSize);
            } else {
                ctx.update(pw, 0, pw.length);
            }
        }

        altResult = ctx.digest();

        /*
         * Start computation of P byte sequence.
         */
        altCtx.reset();
        for (cnt = 0; cnt < pw.length; ++cnt) {
            altCtx.update(pw, 0, pw.length);
        }
        tempResult = altCtx.digest();

        // produce byte sequence P of the same length as the password where
        byte[] pBytes = new byte[pw.length];
        for (cnt = pw.length; cnt >= blockSize; cnt -= blockSize) {
            System.arraycopy(tempResult, 0, pBytes, 0, blockSize);
        }
        System.arraycopy(tempResult, 0, pBytes, 0, cnt);

        /* Start computation of S byte sequence. */
        altCtx.reset();
        for (cnt = 0; cnt < 16 + altResult[0]; ++cnt) {
            altCtx.update(saltBytes, 0, saltLength);
        }
        tempResult = altCtx.digest();

        /* Byte sequence S */
        byte[] sBytes = new byte[saltLength];
        for (cnt = saltLength; cnt >= blockSize; cnt -= blockSize) {
            System.arraycopy(tempResult, 0, sBytes, 0, blockSize);
        }
        System.arraycopy(tempResult, 0, sBytes, 0, cnt);

        /*
         * Repeatedly run the collected hash value through SHA to burn CPU
         * cycles.
         */
        for (cnt = 0; cnt < rounds; ++cnt) {
            ctx.reset();

            if ((cnt & 1) != 0) {
                ctx.update(pw, 0, pw.length);
            } else {
                ctx.update(altResult, 0, altResult.length);
            }

            if ((cnt % 3) != 0) {
                ctx.update(saltBytes, 0, saltLength);
            }

            if ((cnt % 7) != 0) {
                ctx.update(pw, 0, pw.length);
            }

            if ((cnt & 1) != 0) {
                ctx.update(altResult, 0, altResult.length);
            } else {
                ctx.update(pw, 0, pw.length);
            }

            altResult = ctx.digest();
        }

        /* Now make the output string */
        outputString.append(magic);
        if (customRounds) {
            outputString.append("rounds=" + rounds + "$");
        }
        outputString.append(saltText.substring(0, saltLength));
        outputString.append("$");

        if (size == 256) {
            outputString.append(b64From24Bit(altResult[0], altResult[10], altResult[20], 4));
            outputString.append(b64From24Bit(altResult[21], altResult[1], altResult[11], 4));
            outputString.append(b64From24Bit(altResult[12], altResult[22], altResult[2], 4));
            outputString.append(b64From24Bit(altResult[3], altResult[13], altResult[23], 4));
            outputString.append(b64From24Bit(altResult[24], altResult[4], altResult[14], 4));
            outputString.append(b64From24Bit(altResult[15], altResult[25], altResult[5], 4));
            outputString.append(b64From24Bit(altResult[6], altResult[7], altResult[26], 4));
            outputString.append(b64From24Bit(altResult[27], altResult[28], altResult[17], 4));
            outputString.append(b64From24Bit(altResult[18], altResult[19], altResult[8], 4));
            outputString.append(b64From24Bit(altResult[9], altResult[30], altResult[29], 4));
            outputString.append(b64From24Bit((byte) 0, altResult[31], altResult[30], 3));
        } else {
            outputString.append(b64From24Bit(altResult[0], altResult[21], altResult[42], 4));
            outputString.append(b64From24Bit(altResult[22], altResult[43], altResult[1], 4));
            outputString.append(b64From24Bit(altResult[44], altResult[2], altResult[23], 4));
            outputString.append(b64From24Bit(altResult[3], altResult[24], altResult[45], 4));
            outputString.append(b64From24Bit(altResult[25], altResult[46], altResult[4], 4));
            outputString.append(b64From24Bit(altResult[47], altResult[5], altResult[26], 4));
            outputString.append(b64From24Bit(altResult[6], altResult[27], altResult[48], 4));
            outputString.append(b64From24Bit(altResult[28], altResult[49], altResult[7], 4));
            outputString.append(b64From24Bit(altResult[50], altResult[8], altResult[29], 4));
            outputString.append(b64From24Bit(altResult[9], altResult[30], altResult[51], 4));
            outputString.append(b64From24Bit(altResult[31], altResult[52], altResult[10], 4));
            outputString.append(b64From24Bit(altResult[53], altResult[11], altResult[32], 4));
            outputString.append(b64From24Bit(altResult[12], altResult[33], altResult[54], 4));
            outputString.append(b64From24Bit(altResult[34], altResult[55], altResult[13], 4));
            outputString.append(b64From24Bit(altResult[56], altResult[14], altResult[35], 4));
            outputString.append(b64From24Bit(altResult[15], altResult[36], altResult[57], 4));
            outputString.append(b64From24Bit(altResult[37], altResult[58], altResult[16], 4));
            outputString.append(b64From24Bit(altResult[59], altResult[17], altResult[38], 4));
            outputString.append(b64From24Bit(altResult[18], altResult[39], altResult[60], 4));
            outputString.append(b64From24Bit(altResult[40], altResult[61], altResult[19], 4));
            outputString.append(b64From24Bit(altResult[62], altResult[20], altResult[41], 4));
            outputString.append(b64From24Bit((byte) 0, (byte) 0, altResult[63], 2));
        }

        /* Don't leave anything around in vm they could use. */
        memset(altResult);

        return outputString.toString();
    }

    private static String b64From24Bit(byte b2, byte b1, byte b0, int n) {
        long l = (byteToUnsigned(b2) << 16) | (byteToUnsigned(b1) << 8) | byteToUnsigned(b0);
        return (cryptTo64(l, 4));
    }

    private static int byteToUnsigned(byte aByte) {
        return aByte & 0xFF;
    }

    public static boolean verifySHAPassword(String plaintext, String ciphertext) throws NoSuchAlgorithmException {
        if (ciphertext.charAt(0) != '$' || (ciphertext.charAt(1) != '5' && ciphertext.charAt(1) != '6')
                        || ciphertext.charAt(2) != '$' || ciphertext.length() < 5) {
            return false;
        }

        int size = ciphertext.charAt(1) == '5' ? 256 : 512;

        StringBuilder salt = new StringBuilder(16);
        int idx = ciphertext.indexOf('$', 3);
        for (int i = 3; i < idx; i++) {
            salt.append(ciphertext.charAt(i));
        }

        // Encrypt the plaintext using the salt
        try {
            String our_ciphertext = crypt_sha(plaintext.getBytes("UTF8"), salt.toString(), size);
            return our_ciphertext.equals(ciphertext);
        } catch (UnsupportedEncodingException uee) {
            // ignore
            return false;
        }
    }

    public static String shaCrypt(String password, String charset, int size) throws NoSuchAlgorithmException {
        char saltChars[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789./".toCharArray();
        int numSaltChars = saltChars.length;

        // Generate a random salt
        StringBuilder salt = new StringBuilder(16);
        SecureRandom rand = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            salt.append(saltChars[rand.nextInt(Integer.MAX_VALUE) % numSaltChars]);
        }

        try {
            if (password == null) {
                password = "";
            }
            String encrypted = crypt_sha(password.getBytes(charset), salt.toString(), size);
            return encrypted;
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }
}
