package com.identity4j.connector.script.ssh;

import java.io.IOException;

import com.identity4j.util.expect.ExpectTimeoutException;

public interface SshCommand {
	boolean expect(String pattern, boolean consumeRemainingLine, long timeout) throws ExpectTimeoutException, IOException;

	int drainAndWaitForExit() throws IOException;

    int closeAndExit() throws IOException;

	void ctrlD() throws IOException;

	int getExitCode();

	boolean isRunning() throws IOException;

	int read() throws IOException, ExpectTimeoutException;
	
	int read(long timeout) throws IOException, ExpectTimeoutException;

	void typeAndReturn(String text) throws IOException;

}