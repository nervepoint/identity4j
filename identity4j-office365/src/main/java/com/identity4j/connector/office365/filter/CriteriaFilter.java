package com.identity4j.connector.office365.filter;

/*
 * #%L
 * Identity4J OFFICE 365
 * %%
 * Copyright (C) 2013 - 2020 LogonBox
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
	private Filter value;
	private String op;

	public CriteriaFilter(String name, Filter value) {
		this("eq", name, value);
	}

	public CriteriaFilter(String op, String name, Filter value) {
		this.op = op;
		this.name = name;
		this.value = value;
	}

	@Override
	public String encode() {
		StringBuilder bui = new StringBuilder();
		bui.append(name);
		bui.append(' ');
		bui.append(op);
		bui.append(' ');
		bui.append(value.encode());
		return bui.toString();
	}

	@Override
	public String toString() {
		return getClass().getName() + " [name=" + name + ", value=" + value + ", op=" + op + "]";
	}

}
