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