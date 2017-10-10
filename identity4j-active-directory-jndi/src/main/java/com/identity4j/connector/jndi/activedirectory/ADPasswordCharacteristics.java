package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
