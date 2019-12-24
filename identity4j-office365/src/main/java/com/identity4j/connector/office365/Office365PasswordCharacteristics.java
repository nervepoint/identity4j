package com.identity4j.connector.office365;

/*
 * #%L
 * Identity4J OFFICE 365
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

import java.util.HashMap;
import java.util.Map;

import com.identity4j.util.passwords.PasswordCharacteristics;

/**
 * Represents password characteristics for Office 365 Users.
 * <br />
 * It informs client about minimum maximum length of passwords and different combinations
 * it can have.
 * <br />
 * Refer <a href="http://technet.microsoft.com/en-us/library/jj943764.aspx">Office 365 Password Policies</a> for more details.
 * 
 * @author gaurav
 *
 */
public class Office365PasswordCharacteristics implements PasswordCharacteristics {

	private static final long serialVersionUID = 1919021394879203611L;
	
	private Office365PasswordCharacteristics(){}
	
	/**
     * Singleton instance holder
     * 
     * @author gaurav
     *
     */
	private static class LazyHolder {
		private static final Office365PasswordCharacteristics INSTANCE = new Office365PasswordCharacteristics();
	}
 
	public static Office365PasswordCharacteristics getInstance() {
		return LazyHolder.INSTANCE;
	}

	@Override
	public float getVeryStrongFactor() {
		return 1;
	}

	@Override
	public int getMinimumSize() {
		return 8;
	}

	@Override
	public int getMaximumSize() {
		return 16;
	}

	@Override
	public int getRequiredMatches() {
		return 3;
	}

	@Override
	public int getMinimumLowerCase() {
		return 1;
	}

	@Override
	public int getMinimumUpperCase() {
		return 1;
	}

	@Override
	public int getMinimumDigits() {
		return 1;
	}

	@Override
	public int getMinimumSymbols() {
		return 1;
	}

	@Override
	public char[] getSymbols() {
		return null;
	}

	@Override
	public boolean isDictionaryWordsAllowed() {
		return false;
	}

	@Override
	public boolean isContainUsername() {
		return false;
	}

	@Override
	public Map<String, String> getAttributes() {
		return new HashMap<String,String>();
	}

	@Override
	public int getHistorySize() {
		return 0;
	}

	@Override
	public boolean isAdditionalAnalysis() {
		return false;
	}

	@Override
	public float getMinStrength() {
		return 0;
	}

}
