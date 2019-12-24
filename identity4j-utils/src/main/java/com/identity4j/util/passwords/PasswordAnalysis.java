package com.identity4j.util.passwords;

public class PasswordAnalysis {

	private float strength;
	private String warning;
	private String[] suggestions;

	public PasswordAnalysis() {
	}

	public PasswordAnalysis(float strength) {
		this.strength = strength;
	}

	public float getStrength() {
		return strength;
	}

	public void setStrength(float strength) {
		this.strength = strength;
	}

	public String getWarnings() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public String[] getSuggestions() {
		return suggestions;
	}

	public void setSuggestions(String[] suggestions) {
		this.suggestions = suggestions;
	}
}
