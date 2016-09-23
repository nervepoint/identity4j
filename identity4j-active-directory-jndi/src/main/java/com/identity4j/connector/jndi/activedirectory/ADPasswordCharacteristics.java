package com.identity4j.connector.jndi.activedirectory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.identity4j.util.passwords.PasswordCharacteristics;

public class ADPasswordCharacteristics implements PasswordCharacteristics, Serializable {

	private static final long serialVersionUID = 2667909000209390494L;
	
	private boolean complex;
	private int minLength;
	private Map<String, String> attributes = new HashMap<String, String>();
	int precedence;
	String commonName;
	String dn;
	
	public ADPasswordCharacteristics(boolean complex, 
			int minLength, 
			int passwordHistoryLength, 
			int maximumPasswordAge,
			int minimumPasswordAge,
			int precedence,
			String commonName,
			String dn) {
		this.complex = complex;
		this.minLength = minLength;
		this.precedence = precedence;
		this.commonName = commonName;
		this.dn = dn;
		
		attributes.put("activeDirectory." + ActiveDirectoryConnector.PWD_HISTORY_LENGTH, String.valueOf(passwordHistoryLength));
		attributes.put("activeDirectory." + ActiveDirectoryConnector.MAXIMUM_PASSWORD_AGE_ATTRIBUTE,
			String.valueOf(maximumPasswordAge));
		attributes.put("activeDirectory." + ActiveDirectoryConnector.MINIMUM_PASSWORD_AGE_ATTRIBUTE,
			String.valueOf(minimumPasswordAge));
		attributes.put("activeDirectory.cn", commonName);
		attributes.put("activeDirectory.precedence", String.valueOf(precedence));
	}

	public int getMaximumAge() {
		return Integer.parseInt(attributes.get("activeDirectory." + ActiveDirectoryConnector.MAXIMUM_PASSWORD_AGE_ATTRIBUTE));
	}
	
	public int getMinimumAge() {
		return Integer.parseInt(attributes.get("activeDirectory." + ActiveDirectoryConnector.MINIMUM_PASSWORD_AGE_ATTRIBUTE));
	}
	
	public int getPriority() {
		return Integer.parseInt(attributes.get("activeDirectory.precedence"));
	}
	
	@Override
	public float getVeryStrongFactor() {
		// TODO 1?
		return 2;
	}

	@Override
	public int getMinimumSize() {
		return minLength;
	}

	@Override
	public int getMaximumSize() {
		// http://exchangepedia.com/2007/01/what-is-the-real-maximum-password-length.html
		// http://www.winvistatips.com/max-password-length-t696731.html
		// Others, all giving different answers
		return 127;
	}

	@Override
	public int getMinimumLowerCase() {
		return complex ? 1 : 0;
	}

	@Override
	public int getMinimumUpperCase() {
		return complex ? 1 : 0;
	}

	@Override
	public int getMinimumDigits() {
		return complex ? 1 : 0;
	}

	@Override
	public int getMinimumSymbols() {
		return complex ? 1 : 0;
	}

	@Override
	public char[] getSymbols() {
		return null;
	}

	@Override
	public boolean isDictionaryWordsAllowed() {
		return true;
	}

	@Override
	public int getRequiredMatches() {
		return complex ? 3 : 0;
	}

	@Override
	public boolean isContainUsername() {
		return false;
	}

	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public int getHistorySize() {
		return Integer.parseInt(attributes.get("activeDirectory." + ActiveDirectoryConnector.PWD_HISTORY_LENGTH));
	}
	
	public String getDN() {
		return dn;
	}

	public String getCommonName() {
		return commonName;
	}
}
