package com.identity4j.util.crypt.impl;

import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.nss.NssTokenDatabase;

public class Base64FIPSEncoderTest extends AbstractEncoderTest {

    static {
        try {

            /*
             * We want repeatable results, so must initialize the token database
             * with a fixed passphrase and salt
             * 
             * TODO this does not work :( something else must add some randomness?
             */
            byte[] noise = new byte[128];
            byte[] passphrase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456".getBytes("US-ASCII");
            new NssTokenDatabase(noise, passphrase);
        } catch (Exception e) {
            System.err.println("Failed to initialize token database correctly");
            e.printStackTrace();
        }
    }

    public Base64FIPSEncoderTest() throws Exception {
        super(Base64FIPSEncoder.ID, true, false);
        setExpectedHashes(new byte[][] { "Y0dXL0JGczF6ZUVoa09UTGZVRHBFN1ZPbzZuQjhPYXlFa01CMEZjdy9GUzVtZXAyRmx5UXBmL0dDUU5EdmNHb1dGM0d2WDBoa2hBWS9lTzJQTVoxalh4Rmx4c29iR0hkMHhFaGxwa3RVQStxQjVUTVRpdnNsYWtHZ3BORVljY3VUVkQ0RU02dHI0bENUZUh1U3lsMzZsVFc4Lyt1Q0piTU5yMXN0T3pJVm1pcmJuVFVrU3dNTmltR2I5eEJ0NGxFWDl1SnBnVjJMVnJNdWRnc2lxWEEyWkJrVEZNRUE1aitwcnlDN0VSeDlxaTNEM0lONjZIWUNFNzRySHhHNEMxeFRPd29BODh2M2hwdzdnbzkxQy9JRkZYVGhHemhxeUxBUE1CV0xyU2hNTThlUGsxSiszdnpLczdwZXhJQmJBVDI5a3VGck5hSUFjVTc5eitvZjZNa25RPT0=".getBytes("UTF-8"),
                        "b2g4UTZpY1FtY3dOQTVqakptMGlpMk5WYy9nazAvdE9YdS9tNTBweDYxaXJoZ3Z1ZmY3TlVZVW9CaEVUVzh4NmZxdHQ4U3RyWTVWVU9wK1B6clFxcXdjZnB5Y2p6VzJINy9LZXo2eWwrMlpMdzdwOXJRcnlHRkc0Y2M1bnVjc0x3QnA2WlNXSDUvZ2prUk9qaW9rVkt1Y2NEWjZTeTJxZXh0aWMxTjRZeXRRTEMwejFaTXJ0R3RzWDl6cFQ1eGU5MXFqcWJuSElrdXBpNERxMnlqNUVnditKM2ZsbkJqQmtZdTF1eW1Iajg5T1RmQi9RNWdhVk4xS3RZelgvVmVodFl5WU9yaTRUcytEVExLSEJVdndIdmhoK1Q2ODY3dTRnMUxWckdiM2IreVU4MmtKK2xWUTFjT20xNkQrNDdMT2NUSFJGLy9MdVZXOWFhWXFkYTJQSU53PT0=".getBytes("UTF-8"),
                        "a3hTaCt0Z21ETXJXVnprZnIzcDg0eE5NN2g1L0dLYzZ2RHFJam9QMWNiT1NMZEoyOEUvaWkxZkd0SWN2U1FJY25uVU1XOXc0bkpSaVozLytRTWtHK1dZMlFUSGxTYlZ5c2JKWW43UElQT1U2ZGlRQUhiMStMdldnWE9XaWVoV2x0NTNKemVJdUxjSG9OSW5sWkNBTStRaTJTYnBQYUg0cTAxT1ppUGozcDN3OHdhSzRlRFJEdVFveTg1N1c5MmoxdXh6b2diTmJocVgrVVJtazk3VEljZUdMRmovZjVERC8vZXV0bkxRcmQrclYydGdzYmR5WDVjeDc5bk9VZzdsbmtCMGtoVjhaaW9xY21NQ21sakQxdDhwcFdwNXU3RU9iaEtZanBBZ3BIbGgrN2lvZVpUTm13Y1JPdkpTWnh5RXowalZCbmJ2YW43S0ZzeFlzVmhBZTRBPT0="
                                        .getBytes("UTF-8") });
    }
}
