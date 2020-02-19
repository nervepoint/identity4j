package com.identity4j.connector.jndi.directory.filter;

/*
 * #%L
 * Idenity4J LDAP Directory JNDI
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

public abstract class CriteriaFilter implements Filter {

	private String name;
	private String value;
	private String op;

	public CriteriaFilter(String name, String value) {
		this("=", name, value);
	}

	public CriteriaFilter(String op, String name, String value) {
		this.op = op;
		this.name = name;
		this.value = value;
	}

	@Override
	public String encode() {
		StringBuilder bui = new StringBuilder("(");
		bui.append(name);
		bui.append(op);
		bui.append(value);
		bui.append(")");
		return bui.toString();
	}

	protected static String escape(String name) {
		return escape(name, true);
	}

	protected static String escape(String name, boolean escapeWildcard) {
		name = name.replace("(", "\\28").replace("|", "\\7c").replace("<", "\\3c").replace("/", "\\2f")
				.replace(")", "\\29").replace("~", "\\7e").replace("\\", "\\5c").replace("&", "\\26")
				.replace(">", "\\3e");
		if (escapeWildcard)
			name = name.replace("*", "\\2a");
		return name;
	}

	@Override
	public String toString() {
		return getClass().getName() + " [name=" + name + ", value=" + value + ", op=" + op + "]";
	}

}
