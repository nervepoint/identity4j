package com.identity4j.connector.unix;

import java.io.UnsupportedEncodingException;

import org.junit.Ignore;

import com.identity4j.connector.unix.UnixSHA512Encoder;
import com.identity4j.util.crypt.AbstractEncoderTest;
import com.identity4j.util.crypt.impl.DefaultEncoderManager;

@Ignore
// TODO This encoder doesn't work yet
public class UnixSHA512EncoderTest extends AbstractEncoderTest {
	static {
		DefaultEncoderManager.getInstance().addEncoder(new UnixSHA512Encoder());
	}

	final static String PW1 = "Hello world!";
	final static String HASH1 = "$6$saltstring$svn8UoSVapNtMuq1ukKS4tPQd8iKwSMHWjl/O817G3uBnIFNjnQJuesI68u4OTLiBFdcbYEdFCoEOfaS35inz1";
	final static String SALT1 = "$6$saltstring";

	public UnixSHA512EncoderTest() throws UnsupportedEncodingException {
		super(UnixSHA512Encoder.ID, new String[] { PW1 }, new byte[][] { HASH1.getBytes("UTF-8") }, new byte[][] { SALT1
			.getBytes("UTF-8") }, null, false, true);
	}

}
