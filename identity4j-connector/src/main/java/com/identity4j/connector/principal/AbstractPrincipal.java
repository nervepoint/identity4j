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


import java.util.Map;

import com.identity4j.util.MultiMap;

public class AbstractPrincipal implements Principal {

	private static final long serialVersionUID = 3312929553688315012L;

	private final String guid;
    private final String principalName;
    private final MultiMap attributes = new MultiMap();
    private boolean system;
    private Role[] roles = new Role[0];

    /**
     * @param guid
     * @param principalName
     */
    public AbstractPrincipal(String guid, String principalName) {
        this.guid = guid;
        this.principalName = principalName;
    }
    
    public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}

	public final String getPrincipalName() {
        return principalName;
    }

    public final String getGuid() {
        return guid;
    }

    public final void setAttribute(String name, String value) {
        attributes.set(name, new String[] { value });
    }

    public final void setAttribute(String name, String... values) {
        attributes.set(name, values);
    }

    @Override
    public final void setAttributes(Map<String, String[]> attributes) {
        this.attributes.clear();
        this.attributes.setAll(attributes);
    }

    public final String getAttributeOrDefault(String name, String defaultValue) {
        return attributes.getStringOrDefault(name, defaultValue);
    }

    public final String getAttribute(String name) {
        return attributes.getString(name);
    }
    
    public final String[] getAttributes(String name) {
        return attributes.getStringArray(name);
    }

    public final Map<String, String[]> getAttributes() {
        return attributes.toMap();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("[principalName='").append(getPrincipalName());
        builder.append("', guid='").append(getGuid()).append("']");
        return builder.toString();
    }

    @Override
    public final int hashCode() {
        return getGuid() == null ? -1 : 13 * getGuid().hashCode();
    }

    public final int compareTo(Role role) {
        return getPrincipalName().compareTo(role.getPrincipalName());
    }

	@Override
	public Role[] getRoles() {
		return roles;
	}

	@Override
	public void setRoles(Role[] roles) {
		this.roles = roles;		
	}
}
