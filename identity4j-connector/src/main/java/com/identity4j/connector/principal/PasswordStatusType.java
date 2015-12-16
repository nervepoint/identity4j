package com.identity4j.connector.principal;

/**
 * Represents that status type
 */
public enum PasswordStatusType {

	//
	// WARNING
	//
	// DO NOT CHANGE THE INDEXES OF THESE CONSTANTS, JUST APPEND NEW ONES
	// THIS IS BECAUSE CURRENTLY, THE ENUM TYPE IS STORED BY HIBERNATE AS AN INT,
	// NOT A STRING AS IT SHOULD HAVE BEEN
	//
	//
	
	
    /**
     * The password may not yet be changed by the user (admin may change). Note,
     * this is not the same thing as an account lock. It would usually mean
     * that the password has been changed recently, and the connector has
     * rules in place to not allow further changes too soon.
     */
    locked,
    /**
     * The password is up to date, but may be changed
     */
    upToDate,
    /**
     * The password is near expiry and should be changed
     */
    nearExpiry,
    /**
     * The password is expired and must be changed
     */
    expired,
    /**
     * The password should be changed at next logon for some reason other
     * than expire. For example, the adminstrator may have set the change at
     * next logon. Connectors that do not support changeAtNextLogon may use
     * expire dates to achieve the same effect
     */
    changeRequired, 
    /**
     * Password change may never be changed at all by the user. This status
     * is also set for users of 'service accounts', i.e. the account 
     * Nervepoint itself uses for changing passwords (which may not be
     * changed)
     */
    noChangeAllowed,
    /**
     * The password is set to never expire.
     */
    neverExpires
}