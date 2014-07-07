/* HEADER */
package com.identity4j.util.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

/**
 * A validator implementation that checks the supplied value is a valid ip
 * address or host name. This validator also verifies the supplied port, if
 * {@link #INCLUDES_PORT} is set to true. If the {@link #INCLUDES_PORT} is not
 * specified, the validator uses the default value.
 */
public class IpAddressValidator extends AbstractSingleValueValidator {
	private static final char PORT_SEPARATOR = ':';
	/**
	 * Parameter which when supplied, specifies the if the ip address supplied
	 * can include a port. If this value is not supplied, the validator defaults
	 * to a allowing the port to be included.
	 */
	public static final String INCLUDES_PORT = "INCLUDES_PORT";

	private final boolean includesPort;

	/**
	 * Constructor.
	 * 
	 * @param parameters parameters
	 */
	public IpAddressValidator(MultiMap parameters) {
		super(parameters);
		includesPort = parameters.getBooleanOrDefault(INCLUDES_PORT, true);
	}

	@Override
	final Collection<ValidationError> validate(ValidationContext context, String value) {
		String ipAddress = value;
		if (includesPort) {
			int indexOf = value.lastIndexOf(PORT_SEPARATOR);
			ipAddress = indexOf == -1 ? value : value.substring(0, indexOf);
		}

		Collection<ValidationError> errors = new ArrayList<ValidationError>();
		if (StringUtil.isNullOrEmpty(ipAddress) || !isValidHostOrIpAddress(ipAddress)) {
			errors.add(new ValidationError("ipAddress.value.invalid", context, ipAddress));
		}

		if (includesPort) {
			int indexOf = value.lastIndexOf(PORT_SEPARATOR);
			if (indexOf != -1) {
				String potentialPort = value.substring(indexOf + 1, value.length());
				try {
					Integer port = Integer.valueOf(potentialPort);
					if (port < 0 || port > 65535) {
						errors.add(new ValidationError("ipAddress.port.value.invalid", context, ipAddress));
					}
				} catch (NumberFormatException nfe) {
					errors.add(new ValidationError("ipAddress.port.value.invalid", context, ipAddress));
				}
			}
		}
		return errors;
	}

	private boolean isValidHostOrIpAddress(String value) {
		/*
		 * FIXME (Trac #) This is weak and really not a valid test. It will use
		 * the platforms name resolution which may well return incorrect results
		 * anyway. For example, the 'invalid' address used by this test is
		 * originally used 888.888.888.888. This is not a valid IP address but
		 * it is a valid host name.
		 * 
		 * The validation should not by default test if this a valid host name
		 * on the network, that is likely to irrelevant. It should only test it
		 * is well formed according to the configured rules.
		 * 
		 * The validator should be capable of specifying whether IPv4 addresses,
		 * IPv6 addresses or FQDN's are valid in the context it is being used.
		 * 
		 * So I can run a full Maven build, I have changed this to a simple
		 * length test
		 * 
		 * See http://en.wikipedia.org/wiki/Hostname and/or RFC1178
		 */
		// try {
		// InetAddress.getByName(value);
		// return true;
		// } catch (UnknownHostException uhe) {
		// return false;
		// }

		return value.length() > 0 && value.length() < 256 && !(value.startsWith(" ") || value.endsWith(" "));
	}

	public static final boolean isHostName(String ipAddressOrHostName) {
		String[] tokens = ipAddressOrHostName.split("\\.");
		if (tokens.length > 1) {
			return !containsFourNumbericParts(ipAddressOrHostName);
		}
		return false;
	}

	private static boolean containsFourNumbericParts(String ipAddress) {
		int regionCount = 0;
		for (StringTokenizer tokenizer = new StringTokenizer(ipAddress, "."); tokenizer.hasMoreTokens();) {
			try {
				String token = tokenizer.nextToken();
				Integer.parseInt(token);
				regionCount++;
			} catch (NumberFormatException nfe) {
				return false;
			}
		}
		return 4 == regionCount;
	}

	@Override
	public final String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[includesPort='").append(includesPort).append("']");
		return builder.toString();
	}
}