package com.identity4j.util.passwords;

public class PasswordPolicyException extends Exception {
    
	private static final long serialVersionUID = -8948568782955097084L;

	public enum Type {
        tooShort, tooLong, notEnoughLowerCase, notEnoughUpperCase, notEnoughSymbols, notEnoughDigits, containsDictionaryWords, containsUsername, doesNotMatchComplexity,
    }
    
    private Type type;
    private float strength;

    public PasswordPolicyException(Type type, float strength) {
        super();
        this.type = type;
        this.strength = strength;
    }
    
    public Type getType() {
        return type;
    }
    
    public float getStrength() {
        return strength;
    }

}
