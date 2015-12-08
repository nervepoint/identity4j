package com.identity4j.connector.office365;

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

}
