package com.identity4j.util.passwords;

/*
 * #%L
 * Identity4J Utils
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

public class PasswordAnalyser {

	private PasswordDictionaryService dictionaryService;

	public PasswordDictionaryService getDictionaryService() {
		return dictionaryService;
	}

	public void setDictionaryService(PasswordDictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public PasswordAnalysis analyse(Locale locale, String username, char[] password, PasswordCharacteristics characteristics)
			throws PasswordPolicyException {

		// Count up the character types
		int lowerCase = 0;
		int upperCase = 0;
		int digit = 0;
		int other = 0;
		int unmatched = 0;

		// Use the default symbols if the provided characteristics doesn't
		// supply them
		char[] symbols = characteristics.getSymbols();

		String others = new String(symbols);
		for (char ch : password) {
			if (Character.isDigit(ch)) {
				digit++;
			} else if (Character.isLowerCase(ch)) {
				lowerCase++;
			} else if (Character.isUpperCase(ch)) {
				upperCase++;
			} else if (others.indexOf(ch) != -1) {
				other++;
			} else {
				unmatched++;
			}
		}

		/**
		 * Calculate the ideal strength. First determine what would be
		 * considered a strong password. This is a total of all the minimum
		 * character type values multiplied by the <i>Very Strong Password
		 * Factor</i>
		 */

		float idealStrength = characteristics.getMinimumSize();
		idealStrength += Math.max(0, characteristics.getMinimumDigits());
		idealStrength += Math.max(0, characteristics.getMinimumLowerCase());
		idealStrength += Math.max(0, characteristics.getMinimumUpperCase());
		idealStrength += Math.max(0, characteristics.getMinimumSymbols());
		idealStrength *= Math.max(0, characteristics.getVeryStrongFactor());
		idealStrength = Math.max(1, idealStrength);

		// Actual strength is a total of actual counts
		float actualStrength = digit + lowerCase + upperCase + other;

		// Bonus points for unmatched characters
		actualStrength += unmatched;

		// Don't exceed the ideal
		actualStrength = Math.min(idealStrength, actualStrength);

		PasswordAnalysis analysis = new PasswordAnalysis(actualStrength / idealStrength);

		// First check the password is within the size range
		if (characteristics.getMinimumSize() != -1 && password.length < characteristics.getMinimumSize()) {
			throw new PasswordPolicyException(PasswordPolicyException.Type.tooShort, analysis);
		}
		if (characteristics.getMaximumSize() != -1 && password.length > characteristics.getMaximumSize()) {
			throw new PasswordPolicyException(PasswordPolicyException.Type.tooLong, analysis);
		}

		// Check the password doesn't contain the username
		if (username != null && !characteristics.isContainUsername()) {
			if (new String(password).toLowerCase().contains(username.toLowerCase())) {
				throw new PasswordPolicyException(PasswordPolicyException.Type.containsUsername, analysis);
			}
		}

		// Check the password doesn't contain any dictionary words
		if (!characteristics.isDictionaryWordsAllowed()) {
			// Break up the passphrase into what look like words
			StringBuilder bui = new StringBuilder();
			List<String> words = new ArrayList<String>();
			for (char ch : password) {
				if (Character.isLetter(ch)) {
					bui.append(ch);
				} else {
					if (bui.length() > 0) {
						words.add(bui.toString());
						bui.setLength(0);
					}
				}
			}

			// Now look for those words
			for (String word : words) {
				// Minimum of 4 letter words
				if (word.length() > 3) {
					if (dictionaryService != null && dictionaryService.containsWord(locale, word)) {
						throw new PasswordPolicyException(PasswordPolicyException.Type.containsDictionaryWords,
								analysis);
					}
				}
			}
		}

		// Check against policy
		if (characteristics.getRequiredMatches() == 4) {
			check(PasswordPolicyException.Type.notEnoughDigits, digit, characteristics.getMinimumDigits(), analysis);
			check(PasswordPolicyException.Type.notEnoughLowerCase, lowerCase, characteristics.getMinimumLowerCase(),
					analysis);
			check(PasswordPolicyException.Type.notEnoughUpperCase, upperCase, characteristics.getMinimumUpperCase(),
					analysis);
			check(PasswordPolicyException.Type.notEnoughSymbols, other, characteristics.getMinimumSymbols(), analysis);
		} else {
			int matches = 0;
			if (matches(PasswordPolicyException.Type.notEnoughDigits, digit, characteristics.getMinimumDigits(),
					analysis)) {
				matches++;
			}
			if (matches(PasswordPolicyException.Type.notEnoughLowerCase, lowerCase,
					characteristics.getMinimumLowerCase(), analysis)) {
				matches++;
			}
			if (matches(PasswordPolicyException.Type.notEnoughUpperCase, upperCase,
					characteristics.getMinimumUpperCase(), analysis)) {
				matches++;
			}
			if (matches(PasswordPolicyException.Type.notEnoughSymbols, other, characteristics.getMinimumSymbols(),
					analysis)) {
				matches++;
			}
			if (matches < characteristics.getRequiredMatches()) {
				throw new PasswordPolicyException(PasswordPolicyException.Type.doesNotMatchComplexity, analysis);
			}
		}
		
		if(characteristics.isAdditionalAnalysis()) {
			/* We throw away our own legacy strength calculation and use the one provided by zxcvbn */
			Zxcvbn zxcvbn = new Zxcvbn();
			Strength zstrength = zxcvbn.measure(new String(password));
			analysis.setStrength((float)zstrength.getScore() / 4f); 
			analysis.setSuggestions(zstrength.getFeedback().getSuggestions().toArray(new String[0]));
			analysis.setWarning(zstrength.getFeedback().getWarning());
		}
		
		if(analysis.getStrength() < characteristics.getMinStrength())
			throw new PasswordPolicyException(PasswordPolicyException.Type.doesNotMatchMinimumStrength, analysis);

		return analysis;

	}

	private void check(PasswordPolicyException.Type type, int val, int req, PasswordAnalysis analysis)
			throws PasswordPolicyException {
		if (!matches(type, val, req, analysis)) {
			throw new PasswordPolicyException(type, analysis);
		}
	}

	private boolean matches(PasswordPolicyException.Type type, int val, int req, PasswordAnalysis analysis)
			throws PasswordPolicyException {
		return req == -1 || val >= req;
	}
}
