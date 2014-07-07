package com.identity4j.util.passwords;

public class UNIXPasswordCharacteristics extends DefaultPasswordCharacteristics {

	private static final long serialVersionUID = 1L;
	private String authToken = "UNIX";
	private boolean useCracklib;

	public UNIXPasswordCharacteristics() {
		super();
	}

	public UNIXPasswordCharacteristics(PasswordCharacteristics p) {
		super(p);
	}

	public boolean isUseCracklib() {
		return useCracklib;
	}

	public void setUseCracklib(boolean useCracklib) {
		this.useCracklib = useCracklib;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

}
