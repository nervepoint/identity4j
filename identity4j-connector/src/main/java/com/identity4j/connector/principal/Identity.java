/* HEADER */
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


import java.util.Date;

import com.identity4j.connector.Media;


public interface Identity extends Comparable<Identity>, Principal {

    /**
     * Get the identity's full name.
     * 
     * @return full name
     */
    String getFullName();

    /**
     * Set the identity's full name
     * 
     * @param fullName full name
     */
    void setFullName(String fullName);

    /**
     * If the connector supports password changing, it may return a date the
     * password was last changed. Todays date should be returned if this
     * information is not available so it is assumed the password does not need
     * changing.
     * 
     * @return last password change date
     */
    Date getLastSignOnDate();

    /**
     * Set the last sign on change
     * 
     * @param last sign on date
     */
    void setLastSignOnDate(Date lastSignOnDate);

    /**
     * Get a list of all the roles the identity is assigned
     * 
     * @return roles
     */
    Role[] getRoles();

    /**
     * Set the roles this identity is assigned to
     * 
     * @param roles
     */
    void setRoles(Role[] roles);

    /**
     * Adds a role to this identity.
     * 
     * @param role
     */
    void addRole(Role role);

    /**
     * Adds a role from this identity.
     * 
     * @param role
     */
    void removeRole(Role role);

    /**
     * Returns <tt>true</tt> if the supplied role is assigned to the identity
     * 
     * @param role
     * @return <tt>true</tt> if the supplied role is assigned to the identity
     */
    boolean memberOf(Role role);

    /**
     * Set the password status
     * 
     * @param passwordStatus password status
     */
    void setPasswordStatus(PasswordStatus passwordStatus);

    /**
     * Set the account status. This contains details such as account locking.
     * 
     * @param accountStatus account status
     */
    void setAccountStatus(AccountStatus accountStatus);

    /**
     * Get the password status
     * 
     * @return password status
     */
    PasswordStatus getPasswordStatus();

    /**
     * Get the account status
     * 
     * @return account status
     */
    AccountStatus getAccountStatus();
    
    /**
     * Get the a contact detail for the given type of media.
     * 
     * @param media media
     * @return contact details
     */
    String getAddress(Media media);
    
    /**
     * Return an alternative name for this identity that may be used to identify it, for example
     * Active Directory might return the userPrincipalName here.
     * @return
     */
    String getOtherName();
}