package com.identity4j.util.expect;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Expect {

	protected List<ExpectMatcher> matchers = new ArrayList<ExpectMatcher>();
	protected BufferedInputStream in;
	private OutputStream out;
	private String eol;
	private boolean open = true;
	
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
		
		if(matcher!=null) {
			matchers.add(matcher);
		}
		if(!(matcher instanceof DefaultExpectMatcher)) {
			matchers.add(new DefaultExpectMatcher());
		}
		this.in = new BufferedInputStream(in);
		this.out = out;
		this.eol = eol;
	}

	protected boolean isOpen() {
		return open;
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
		try {
			String chat = chat(pattern, consumeRemainingLine, timeout, maxLines, false);
			boolean matched = chat != null;
			return matched;
		} catch (EOFException e) {
			return false;
		}
	}

	public synchronized boolean maybeExpectNextLine(String pattern, boolean consumeRemainingLine, long timeout)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, consumeRemainingLine, timeout, 1);
	}


	public synchronized boolean maybeExpectNextLine(String pattern, boolean consumeRemainingLine)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, consumeRemainingLine, 0, 1);
	}

	public synchronized boolean maybeExpectNextLine(String pattern)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, false, 0, 1);
	}

	public synchronized boolean maybeExpect(String pattern, boolean consumeRemainingLine, long timeout)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, consumeRemainingLine, timeout, 0);
	}

	public synchronized boolean maybeExpect(String pattern, long timeout)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, false, timeout, 0);
	}
	
	public synchronized boolean maybeExpect(String pattern, boolean consumeRemainingLine)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, consumeRemainingLine, 0, 0);
	}

	public synchronized boolean maybeExpect(String pattern)
			throws ExpectTimeoutException, IOException {
		return maybeExpect(pattern, false, 0, 0);
	}

	public synchronized boolean maybeExpect(String pattern, boolean consumeRemainingLine, long timeout, long maxLines)
			throws ExpectTimeoutException, IOException {
		in.mark(32768);
		try {
			String chat = chat(pattern, consumeRemainingLine, timeout, maxLines, false);
			boolean matched = chat != null;
			if(!matched) {
				in.reset();
			}
			return matched;
		} catch (EOFException e) {
			in.reset();
			return false;
		} catch (ExpectTimeoutException e) {
			in.reset();
			return false;
		}
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

			try {
				if (maxLines > 0 && lines >= maxLines)
					return null;

				int ch = read(timeout);
				if (ch == -1) {
										
					if(line.length() > 0 && matches(line.toString(), pattern)) {
						open = false;
						return line.toString();
					} else {
						throw new EOFException();
					}
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
				if (matches(line.toString(), pattern)) {
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
			} catch (IOException e) {
				open = false;
				throw e;
			}
		}

		throw new ExpectTimeoutException();
	}

	protected boolean matches(String line, String pattern) {
		for(ExpectMatcher m :
			matchers) {
			if(m.matches(line, pattern)) {
				return true;
			}
		}
		return false;
	}
	
	protected int read(long timeout) throws IOException, ExpectTimeoutException {
		
		long startTime = System.currentTimeMillis();
		
		try {
			if(timeout > 0) {
				while(isOpen() && in.available()==0 && System.currentTimeMillis() - startTime < timeout) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
				if(in.available()==-1) {
					return -1;
				}
				/**
				 * What is this? available could easily be zero??????
				 */
				if(in.available()==0) {
					throw new ExpectTimeoutException();
				}
			}
			
			int read = in.read();

			return read;
		
		} catch(EOFException e) {
			if(log.isDebugEnabled()) {
				log.debug("Received EOF exception");
			}
			return -1;
		}
		
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
			ch = read(timeout);
			if (ch == -1 || ch == '\n') {
				if (line.length() == 0 && ch == -1) {
					open = false;
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
		this.in = new BufferedInputStream(in);
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
