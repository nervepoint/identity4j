package com.identity4j.connector.principal;

/*
 * #%L
 * Identity4J Connector
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