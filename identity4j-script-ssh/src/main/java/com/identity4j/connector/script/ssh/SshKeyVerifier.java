package com.identity4j.connector.script.ssh;

import java.util.Set;

public interface SshKeyVerifier {

	boolean verifyKey(String host, int port, String algorithm, int bitLength, byte[] encoded, String fingerprint, byte[] formatted);

	Set<String> getKeyAlgorithms();

	int getCount();
}
