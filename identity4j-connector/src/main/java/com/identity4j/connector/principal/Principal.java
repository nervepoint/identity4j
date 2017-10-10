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


import java.io.Serializable;
import java.util.Map;

/**
 * This interface represents the abstract notion of a principal, which can be
 * used to represent any entity, such as a identity or a role.
 */
public interface Principal extends Serializable {
	
	/**
	 * Get if this is a <code>system</code> principal. Typically this is used to mark the
	 * service account so it can be excluded.
	 * 
	 * @return system principal
	 */
	boolean isSystem();

    /**
     * Get the name of this principal.
     * 
     * @return principal
     */
    String getPrincipalName();

    /**
     * Globally Unique Identifier is a special type of identifier
     * 
     * @return
     */
    String getGuid();

    /**
     * Associates the specified values with the specified attribute name. If the
     * <code>Principal</code> previously contained a mapping for the attribute,
     * the old value is replaced by the specified value.
     * 
     * @param name name with which the specified values are to be associated
     * @param values values to be associated with the specified attribute name
     */
    void setAttribute(String name, String... values);

    /**
     * Returns the value associated with the specified attribute name or the
     * <code>null</code>.
     * 
     * @param name
     * @param defaultValue
     * @return the attributes value
     */
    String getAttribute(String name);
    
    /**
     * Returns the values associated with the specified attribute name or the
     * <code>null</code>.
     * 
     * @param name
     * @param defaultValue
     * @return the attributes value
     */
    String[] getAttributes(String name);

    /**
     * Returns the value associated with the specified attribute name or the
     * supplied defaultValue if it was null.
     * 
     * @param name
     * @param defaultValue
     * @return the attributes value
     */
    String getAttributeOrDefault(String name, String defaultValue);

    /**
     * Set the values of all attributes for this identity.
     * 
     * @param attributes map of attribute values
     */
    void setAttributes(Map<String, String[]> attributes);

    /**
     * @return
     */
    Map<String, String[]> getAttributes();

    /**
     * Get a list of all the roles the principal is assigned
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
}