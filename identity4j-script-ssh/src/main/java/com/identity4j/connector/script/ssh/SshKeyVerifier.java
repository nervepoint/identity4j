package com.identity4j.connector.script.ssh;

public interface SshKeyVerifier {

	boolean verifyKey(String host, int port, String algorithm, int bitLength, byte[] encoded, String fingerprint);

}
