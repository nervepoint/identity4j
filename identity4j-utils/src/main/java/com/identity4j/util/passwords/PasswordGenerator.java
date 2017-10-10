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

import java.util.Arrays;
import java.util.Locale;

public class PasswordGenerator {

	private final static Character[] VOWELS = { 'a', 'e', 'i', 'o', 'u' };

	private PasswordCharacteristics characteristics;

	private int maximumSymbols = -1;
	private int maximumLowerCase = -1;
	private int maximumUpperCase = -1;
	private int maximumDigits = -1;

	private String order = null;

	private PasswordAnalyser analyser;

	public PasswordGenerator() {
	}

	public PasswordGenerator(PasswordAnalyser analyser) {
		this.analyser = analyser;
	}

	public PasswordAnalyser getPasswordAnalyserService() {
		return analyser;
	}

	public void setPasswordAnalyserService(PasswordAnalyser analyser) {
		this.analyser = analyser;
	}

	public PasswordGenerator(PasswordAnalyser analyser, PasswordCharacteristics characteristics) {
		setPasswordCharacteristics(characteristics);
		this.analyser = analyser;
	}

	public void setOrder(char[] order) {
		if (order == null || order.length == 0)
			this.order = null;
		else {
			this.order = new String(order);
		}
	}

	public int getMaximumSymbols() {
		return maximumSymbols;
	}

	public int getMaximumLowerCase() {
		return maximumLowerCase;
	}

	public int getMaximumUpperCase() {
		return maximumUpperCase;
	}

	public int getMaximumDigits() {
		return maximumDigits;
	}

	public char[] generate(Locale locale, String username) {
		if (characteristics == null) {
			throw new IllegalStateException("Characteristics not set");
		}
		if (analyser == null) {
			throw new IllegalStateException("Analyser not set");
		}

		if (characteristics.getMaximumSize() > 0) {
			if (characteristics.getMaximumSize() < characteristics.getMinimumSize()) {
				throw new IllegalArgumentException("Maximum size must be < 1 or >= minimum size");
			}

			// The total of the 4 characters types must not exceed any maximum
			// size
			if ((Math.max(0, characteristics.getMinimumLowerCase()) + Math.max(0, characteristics.getMinimumUpperCase())
					+ Math.max(0, characteristics.getMinimumSymbols())
					+ Math.max(0, characteristics.getMinimumDigits())) > characteristics.getMaximumSize()) {
				throw new IllegalArgumentException(
						"Total of mimimum sizes of characters type must not exceed maximum size of "
								+ characteristics.getMaximumSize());

			}
			// The total of the 4 characters types must not exceed any maximum
			// size
			if ((Math.max(0, maximumLowerCase) + Math.max(0, maximumUpperCase) + Math.max(0, maximumSymbols)
					+ Math.max(0, maximumDigits)) > characteristics.getMaximumSize()) {
				throw new IllegalArgumentException(
						"Total of maximum sizes of characters type must not exceed maximum size of "
								+ characteristics.getMaximumSize());
			}
		}

		int diff = characteristics.getMaximumSize() - characteristics.getMinimumSize();
		int off = (int) Math.round((float) diff * Math.random());
		int targetLength = Math.min(
				characteristics.getMaximumSize() < 1 ? Integer.MAX_VALUE : characteristics.getMaximumSize(),
				characteristics.getMinimumSize() + off);

		StringBuilder pw = new StringBuilder();
		char prevChar = '\0';

		for (char o : (isAllClasses() ? Math.random() > 0.5 ? "DULS".toCharArray() : "ULDS".toCharArray()
				: order.toCharArray())) {
			switch (o) {
			case 'u':
				prevChar = doUpperCase(pw);
				break;
			case 'l':
				prevChar = doLowerCase(pw, prevChar);
				break;
			case 'd':
				prevChar = doDigit(pw);
				break;
			case 's':
				prevChar = doSymbol(pw, prevChar);
				;
				break;
			case 'U':
				prevChar = doAddUpper(targetLength, pw, prevChar);
				break;
			case 'L':
				prevChar = doAddLower(targetLength, pw, prevChar);
				break;
			case 'D':
				prevChar = doAllDigits(pw, targetLength, prevChar);
				break;
			case 'S':
				prevChar = doSymbols(targetLength, pw, prevChar);
				;
				break;
			default:
				throw new IllegalArgumentException("Incorrect password generation format. Contact the administrator.");
			}
		}

		// Analyse the password until it is OK
		int loops = 0;
		while (++loops > 0) {
			if (loops > 10000) {
				throw new Error(
						"Password generator is looping suggesting there is a bug. As a work around, you might try adjusting your password policy. "
								+ toString());
			}
			try {
				analyser.analyse(locale, username, pw.toString().toCharArray(), characteristics);
				if (pw.length() < targetLength) {
					throw new PasswordPolicyException(PasswordPolicyException.Type.tooShort, 0);
				}
				break;
			} catch (PasswordPolicyException pe) {
				if (pe.getType().equals(PasswordPolicyException.Type.tooLong)) {
					pw.deleteCharAt((int) Math.random() * pw.length());
				} else if (pe.getType().equals(PasswordPolicyException.Type.tooShort)) {
					prevChar = appendChar(pw, prevChar);
				} else {
					if (pw.length() > characteristics.getMaximumSize()) {
						pw.deleteCharAt((int) Math.random() * pw.length());
					}
					prevChar = appendChar(pw, prevChar);
				}
			}
		}
		return pw.toString().toCharArray();
	}

	private boolean isClassInUse(char c) {
		return isAllClasses() || order.indexOf(Character.toUpperCase(c)) != -1
				|| order.indexOf(Character.toLowerCase(c)) != -1;
	}

	private boolean isAllClasses() {
		return order == null || order.length() == 0;
	}

	protected char doSymbols(int targetLength, StringBuilder pw, char prevChar) {
		int c = characteristics.getMinimumSymbols();
		if (c != -1 && characteristics.getSymbols() != null && characteristics.getSymbols().length > 0) {
			c = calcCharactersToAdd(c, maximumSymbols, targetLength);
			for (int i = 0; i < c; i++) {
				prevChar = doSymbol(pw, prevChar);
			}
		}
		return prevChar;
	}

	protected char doAddLower(int targetLength, StringBuilder pw, char prevChar) {
		int c = calcCharactersToAdd(characteristics.getMinimumLowerCase(), maximumLowerCase, targetLength);
		for (int i = 0; i < c; i++) {
			prevChar = doLowerCase(pw, prevChar);
		}
		return prevChar;
	}

	protected char doAddUpper(int targetLength, StringBuilder pw, char prevChar) {
		int c = calcCharactersToAdd(characteristics.getMinimumUpperCase(), maximumUpperCase, targetLength);
		for (int i = 0; i < c; i++) {
			prevChar = (char) (65 + (Math.random() * 26));
			pw.append(prevChar);
		}
		return prevChar;
	}

	protected int calcCharactersToAdd(int minimum, int maximum, int targetLength) {
		if (maximum == 0)
			return 0;
		if (maximum == -1)
			maximum = targetLength;
		int min = minimum == 0 ? (int) Math.round((float) targetLength * 0.1) : minimum;
		int c = min + (int) ((float) (maximum - min + 1) * Math.random());
		return c;
	}

	private char doAllDigits(StringBuilder pw, int targetLength, char prevChar) {
		int c = calcCharactersToAdd(characteristics.getMinimumDigits(), maximumDigits, targetLength);
		for (int i = 0; i < c; i++) {
			prevChar = doDigit(pw);
		}
		return prevChar;
	}

	private char doDigit(StringBuilder pw) {
		char generateDigit = generateDigit();
		pw.append(generateDigit);
		return generateDigit;
	}

	private char doSymbol(StringBuilder pw, char prevChar) {
		// Symbol
		char[] symbols = characteristics.getSymbols();
		if (symbols.length > 0) {
			char c = symbols[(int) (Math.random() * symbols.length)];
			pw.append(c);
			return c;
		}
		else
			return prevChar;
	}

	private char appendChar(StringBuilder pw, char prevChar) {
		int i = 0;
		while (i < 9999) {
			switch ((int) (Math.random() * 8)) {
			case 0:
				// Upper case
				if (isClassInUse('U')) {
					prevChar = doUpperCase(pw);
					return prevChar;
				}
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				if (isClassInUse('L')) {
					prevChar = doLowerCase(pw, prevChar);
					i = Integer.MAX_VALUE;
					return prevChar;
				}
				break;
			case 6:
				// Digit
				if (isClassInUse('D')) {
					pw.append(generateDigit());
					return prevChar;
				}
				break;
			default:
				if (isClassInUse('S')) {
					doSymbol(pw, prevChar);
					return prevChar;
				}
				break;
			}
			i++;
		}
		return prevChar;
	}

	private char doUpperCase(StringBuilder pw) {
		char prevChar;
		prevChar = ((char) (65 + (Math.random() * 26)));
		// pw.insert(0, prevChar);
		pw.append(prevChar);
		return prevChar;
	}

	private char doLowerCase(StringBuilder pw, char prevChar) {
		// Lower case
		if (prevChar != '\0' && !isVowel(prevChar)) {
			// Use a vowel if the previous character wasn't a vowel
			prevChar = VOWELS[(int) (Math.random() * VOWELS.length)].charValue();
		} else {
			prevChar = (char) (97 + (Math.random() * 26));
		}

		// Try and insert at the second character to keep at least one
		// upper case character at the start
		// pw.insert(Math.min(pw.length(), 1), String.valueOf(prevChar));
		pw.append(prevChar);
		return prevChar;
	}

	private char generateDigit() {
		return (char) (48 + (Math.random() * 10));
	}

	private boolean isVowel(char ch) {
		return Arrays.asList(VOWELS).contains(Character.valueOf(ch));
	}

	public void setPasswordCharacteristics(PasswordCharacteristics characteristics) {
		this.characteristics = characteristics;

	}

	public PasswordCharacteristics getPasswordCharacteristics() {
		return characteristics;
	}

	public void setMaximumSymbols(int maximumSymbols) {
		this.maximumSymbols = maximumSymbols;
	}

	public void setMaximumLowerCase(int maximumLowerCase) {
		this.maximumLowerCase = maximumLowerCase;
	}

	public void setMaximumUpperCase(int maximumUpperCase) {
		this.maximumUpperCase = maximumUpperCase;
	}

	public void setMaximumDigits(int maximumDigits) {
		this.maximumDigits = maximumDigits;
	}

	@Override
	public String toString() {
		return "PasswordGeneratorServiceImpl [characteristics=" + characteristics + ", maximumSymbols=" + maximumSymbols
				+ ", maximumLowerCase=" + maximumLowerCase + ", maximumUpperCase=" + maximumUpperCase
				+ ", maximumDigits=" + maximumDigits + ", order=" + order + ", analyser=" + analyser + "]";
	}

}
