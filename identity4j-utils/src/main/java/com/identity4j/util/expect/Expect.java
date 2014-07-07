package com.identity4j.util.expect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Expect {

	protected ExpectMatcher matcher = null;
	protected InputStream in;
	private OutputStream out;
	private String eol;

	static Log log = LogFactory.getLog(Expect.class);

	public Expect() {
		this(null, null, null, "\r");
	}
	
	public Expect(ExpectMatcher matcher) {
		this(matcher, null, null, "\r");
	}

	public Expect(InputStream in, OutputStream out) {
		this(null, in, out, "\r");
	}

	public Expect(ExpectMatcher matcher, InputStream in, OutputStream out) {
		this(matcher, in, out, "\r");
	}

	public Expect(ExpectMatcher matcher, InputStream in, OutputStream out, String eol) {
		this.matcher = matcher == null ? new DefaultExpectMatcher() : matcher;
		this.in = in;
		this.out = out;
		this.eol = eol;
	}

	public void setMatcher(ExpectMatcher matcher) {
		this.matcher = matcher;
	}

	public void interrupt() throws IOException {
		type(new String(new char[] { 3 }));
	}

	/**
	 * Type some characters as input on the remote shell.
	 * 
	 * @param string String
	 * @throws IOException
	 */
	public synchronized void type(String string) throws IOException {
		write(string.getBytes());
	}

	/**
	 * Consume the output of the command until the pattern matches. This version
	 * of expect will return with the output at the end of the matched pattern.
	 * 
	 * @param pattern
	 * @return
	 * @throws ShellTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expect(String pattern) throws ExpectTimeoutException, IOException {
		return expect(pattern, false, 0, 0);
	}

	/**
	 * Consume the output of the command until the pattern matches. Use the
	 * consumeRemainingLine variable to indicate if output should start at the
	 * end of the matched pattern (consumeRemainingLine=false) or at the
	 * begninng of the next line (consumeRemainingLine=true)
	 * 
	 * @param pattern
	 * @param consumeRemainingLine
	 * @return
	 * @throws ShellTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expect(String pattern, boolean consumeRemainingLine) throws ExpectTimeoutException, IOException {
		return expect(pattern, consumeRemainingLine, 0, 0);
	}

	/**
	 * Consume the output of the command until the pattern matches. Use the
	 * consumeRemainingLine variable to indicate if output should start at the
	 * end of the matched pattern (consumeRemainingLine=false) or at the
	 * begninng of the next line (consumeRemainingLine=true)
	 * 
	 * @param pattern
	 * @param timeout
	 * @return
	 * @throws ShellTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expect(String pattern, long timeout) throws ExpectTimeoutException, IOException {
		return expect(pattern, false, timeout, 0);
	}

	/**
	 * Consume the output of the command until the pattern matches. This version
	 * of expect will not consume the whole line and will return with the output
	 * at the end of the matched pattern.
	 * 
	 * @param pattern
	 * @param consumeRemainingLine
	 * @param timeout
	 * @param maxLines
	 * @return
	 * @throws ShellTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expect(String pattern, boolean consumeRemainingLine, long timeout) throws ExpectTimeoutException,
			IOException {
		return expect(pattern, consumeRemainingLine, timeout, 0);
	}

	/**
	 * Perform expect on the next line of output only
	 * 
	 * @param pattern
	 * @return
	 * @throws ShellTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expectNextLine(String pattern) throws ExpectTimeoutException, IOException {
		return expect(pattern, false, 0, 1);
	}

	/**
	 * Perform expect on the next line of output only
	 * 
	 * @param pattern
	 * @param consumeRemainingLine
	 * @return
	 * @throws ShellTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expectNextLine(String pattern, boolean consumeRemainingLine) throws ExpectTimeoutException,
			IOException {
		return expect(pattern, consumeRemainingLine, 0, 1);
	}

	/**
	 * Perform expect on the next line of output only
	 * 
	 * @param pattern
	 * @param consumeRemainingLine
	 * @param timeout
	 * @return
	 * @throws ExpectTimeoutException
	 * @throws SshException
	 */
	public synchronized boolean expectNextLine(String pattern, boolean consumeRemainingLine, long timeout)
			throws ExpectTimeoutException, IOException {
		return expect(pattern, consumeRemainingLine, timeout, 1);
	}

	/**
	 * Consume the output of the command until the pattern matches. This version
	 * of expect will not consume the whole line and will return with the output
	 * at the end of the matched pattern.
	 * 
	 * @param pattern
	 * @param consumeRemainingLine
	 * @param timeout
	 * @param maxLines
	 * @return
	 * @throws ExpectTimeoutException
	 */
	public synchronized boolean expect(String pattern, boolean consumeRemainingLine, long timeout, long maxLines)
			throws ExpectTimeoutException, IOException {
		String chat = chat(pattern, consumeRemainingLine, timeout, maxLines, false);
		return chat != null;
	}

	/**
	 * Consume the output of the command until the pattern matches. This version
	 * of expect will not consume the whole line and will return with the output
	 * at the end of the matched pattern.
	 * 
	 * @param pattern
	 * @param consumeRemainingLine
	 * @param timeout
	 * @param maxLines
	 * @param ignoreEmptyLines blank lines are not counted
	 * @return
	 * @throws ExpectTimeoutException
	 */
	public synchronized String chat(String pattern, boolean consumeRemainingLine, long timeout, long maxLines, boolean ignoreEmptyLines)
			throws ExpectTimeoutException, IOException {
		checkIn();

		StringBuffer line = new StringBuffer();
		long time = System.currentTimeMillis();
		long lines = 0;

		while (System.currentTimeMillis() - time < timeout || timeout == 0) {

			if (maxLines > 0 && lines >= maxLines)
				return null;

			int ch = read();
			if (ch == -1) {
				return null;
			}
			else if(ch == Integer.MIN_VALUE) {
				// Timeout
				continue;
			}
			
			if (ch != '\n' && ch != '\r') {
				line.append((char) ch);
			}

			if (log.isDebugEnabled()) {
				log.debug("Checking if '" + line + "' is '" + pattern + "'");
			}
			if (matcher.matches(line.toString(), pattern)) {
				if (log.isDebugEnabled()) {
					log.debug("Matched: [" + pattern + "] " + line.toString());
				}
				if (consumeRemainingLine && ch != '\n' && ch != -1) {
					do {

					} while (ch != '\n' && ch != -1);
				}
				if (log.isDebugEnabled()) {
					log.debug("Matched shell output: " + line.toString());
				}
				return line.toString();
			}

			if (ch == '\n') {
				if(!ignoreEmptyLines) { 
					lines++;
				}
				if (log.isDebugEnabled()) {
					log.debug("Unmatched shell output: " + line.toString());
				}
				line.delete(0, line.length());
			}
		}

		throw new ExpectTimeoutException();
	}

	protected int read() throws IOException {
		int ch = in.read();
		return ch;
	}

	public synchronized String readLine() throws IOException, ExpectTimeoutException {
		return readLine(0);
	}

	public synchronized String readLine(long timeout) throws IOException, ExpectTimeoutException {
		checkIn();
		StringBuffer line = new StringBuffer();
		int ch;

		long time = System.currentTimeMillis();

		do {
			ch = in.read();
			if (ch == -1 || ch == '\n') {
				if (line.length() == 0 && ch == -1) {
					return null;
				}
				return line.toString();
			}
			if (ch != '\n' && ch != '\r') {
				line.append((char) ch);
			}

		} while (System.currentTimeMillis() - time < timeout || timeout == 0);

		throw new ExpectTimeoutException();

	}
	
	public static void main(String[] args) {
		System.out.println("passwd: ".matches("Re-enter new Password: |passwd: "));
	}

	public final InputStream getIn() {
		return in;
	}

	public final void setIn(InputStream in) {
		this.in = in;
	}

	public final OutputStream getOut() {
		return out;
	}

	public final void setOut(OutputStream out) {
		this.out = out;
	}

	public final String getEol() {
		return eol;
	}

	public final void setEol(String eol) {
		this.eol = eol;
	}

	public final ExpectMatcher getMatcher() {
		return matcher;
	}

	/**
	 * Type some characters followed by a carriage return on the remote shell.
	 * 
	 * @param string String
	 * @throws IOException
	 */
	public void typeAndReturn(String string) throws IOException {
		write((string + eol).getBytes());
	}

	/**
	 * Write some data as input on the remote shell.
	 * 
	 * @param bytes
	 * @throws IOException
	 */
	public void write(byte[] bytes) throws IOException {
		checkOut();
		if(log.isDebugEnabled()) {
			log.debug("Sending '" + new String(bytes) + "'");
		}
		out.write(bytes);
		out.flush();
	}

	/**
	 * Write a byte of data as input on the remote shell.
	 * 
	 * @param b
	 * @throws IOException
	 */
	public void type(int b) throws IOException {
		write(new byte[] { (byte) b });
	}

	/**
	 * Send a carriage return to the remote shell.
	 * 
	 * @throws IOException
	 */
	public void carriageReturn() throws IOException {
		write(eol.getBytes());
	}

	private void checkOut() throws IOException {
		if(out == null) {
			throw new IOException("No output stream set");
		}
	}

	private void checkIn() throws IOException {
		if(in == null) {
			throw new IOException("No input stream set");
		}
	}
}
