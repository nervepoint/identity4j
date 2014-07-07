package com.identity4j.connector.principal;

/**
 * Represents that status type
 */
public enum AccountStatusType {
	//
	// WARNING
	//
	// DO NOT CHANGE THE INDEXES OF THESE CONSTANTS, JUST APPEND NEW ONES
	// THIS IS BECAUSE CURRENTLY, THE ENUM TYPE IS STORED BY HIBERNATE AS AN INT,
	// NOT A STRING AS IT SHOULD HAVE BEEN
	//
	//

	
    /**
     * The account is locked.
     */
    locked,
    /**
     * The account is unlocked, enabled and usable
     */
    unlocked,
    /**
     * The account is expired and no longer usable
     */
    expired,
    /**
     * The account is disabled and not usable
     */
    disabled,
}