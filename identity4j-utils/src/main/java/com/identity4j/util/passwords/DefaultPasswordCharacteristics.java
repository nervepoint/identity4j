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

import java.util.HashMap;
import java.util.Map;

import com.identity4j.util.Util;

public class DefaultPasswordCharacteristics implements PasswordCharacteristics {

	public final static char[] DEFAULT_SYMBOLS = { '!', '"', '$', '\u00a3', '%', '^', '&', '*', '(', ')', '_', '-', '=', '+', '[',
		'{', ']', '}', ':', ';', '\'', '@', '~', '#', ',', '<', '.', '>', '|', '\\', '?' };

	private static final long serialVersionUID = 1L;
	private boolean dictionaryWordsAllowed = false;
	private int maximumSize = 255;
	private int historySize = 0;
	private int minimumSize = 8;
	private int minimumLowerCase = 0;
	private int minimumUpperCase = 0;
	private int minimumSymbols = 0;
	private int minimumDigits = 0;
	private int requiresMatches = 4;
	private float veryStrongFactor = 2.0f;
	private boolean containUsername;
	private Map<String, String> attributes = new HashMap<String, String>();
	private char[] symbols = DEFAULT_SYMBOLS;
	private boolean additionalAnalysis = false;
	private float minStrength;

	public DefaultPasswordCharacteristics() {
	}

	public DefaultPasswordCharacteristics(PasswordCharacteristics p) {
		dictionaryWordsAllowed = p.isDictionaryWordsAllowed();
		maximumSize = p.getMaximumSize();
		minimumSize = p.getMinimumSize();
		minimumLowerCase = p.getMinimumLowerCase();
		minimumUpperCase = p.getMinimumUpperCase();
		minimumSymbols = p.getMinimumSymbols();
		minimumDigits = p.getMinimumDigits();
		requiresMatches = p.getRequiredMatches();
		veryStrongFactor = p.getVeryStrongFactor();
		containUsername = p.isContainUsername();
		historySize = p.getHistorySize();
		symbols = p.getSymbols();
		attributes.putAll(p.getAttributes());
		additionalAnalysis = p.isAdditionalAnalysis();
		minStrength = p.getMinStrength();
	}

	public float getMinStrength() {
		return minStrength;
	}

	public void setMinStrength(float minStrength) {
		this.minStrength = minStrength;
	}

	public boolean isAdditionalAnalysis() {
		return additionalAnalysis;
	}

	public void setAdditionalAnalysis(boolean additionalAnalysis) {
		this.additionalAnalysis = additionalAnalysis;
	}

	public int getHistorySize() {
        return historySize;
    }

    public void setHistorySize(int historySize) {
        this.historySize = historySize;
    }

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public float getVeryStrongFactor() {
		return veryStrongFactor;
	}

	public void setVeryStrongFactor(float veryStrongFactor) {
		this.veryStrongFactor = veryStrongFactor;
	}

	public char[] getSymbols() {
		return symbols;
	}

	public void setSymbols(char[] symbols) {
		this.symbols = symbols;
	}

	public boolean isDictionaryWordsAllowed() {
		return dictionaryWordsAllowed;
	}

	public void setDictionaryWordsAllowed(boolean dictionaryWordsAllowed) {
		this.dictionaryWordsAllowed = dictionaryWordsAllowed;
	}

	public int getMaximumSize() {
		return maximumSize;
	}

	public void setMaximumSize(int maximumSize) {
		this.maximumSize = maximumSize;
	}

	public int getMinimumSize() {
		return minimumSize;
	}

	public void setMinimumSize(int minimumSize) {
		this.minimumSize = minimumSize;
	}

	public int getMinimumLowerCase() {
		return minimumLowerCase;
	}

	public void setMinimumLowerCase(int minimumLowerCase) {
		this.minimumLowerCase = minimumLowerCase;
	}

	public int getMinimumUpperCase() {
		return minimumUpperCase;
	}

	public void setMinimumUpperCase(int minimumUpperCase) {
		this.minimumUpperCase = minimumUpperCase;
	}

	public int getMinimumSymbols() {
		return minimumSymbols;
	}

	public void setMinimumSymbols(int minimumSymbols) {
		this.minimumSymbols = minimumSymbols;
	}

	public int getMinimumDigits() {
		return minimumDigits;
	}

	public void setMinimumDigits(int minimumDigits) {
		this.minimumDigits = minimumDigits;
	}

	public int getRequiredMatches() {
		return requiresMatches;
	}

	public int getRequiresMatches() {
		return requiresMatches;
	}

	public boolean isContainUsername() {
		return containUsername;
	}

	public void setContainUsername(boolean containUsername) {
		this.containUsername = containUsername;
	}

	public void setRequiresMatches(int requiresMatches) {
		this.requiresMatches = requiresMatches;
	}

	@Override
	public String toString() {
		return Util.fromObject(this);
	}

}
