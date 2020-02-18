package com.identity4j.connector.office365.filter;

import java.util.LinkedHashSet;
import java.util.Set;

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

public abstract class AbstractJunction implements Junction {

	protected Set<Filter> filters = new LinkedHashSet<>();

	private String op;

	protected AbstractJunction(String op) {
		this.op = op;
	}

	@Override
	public final Junction add(Filter filter) {
		filters.add(filter);
		return this;
	}

	@Override
	public String encode() {
		StringBuilder b = new StringBuilder();
		if (filters.isEmpty())
			throw new IllegalStateException("Empty junction.");
		for (Filter f : filters) {
			if (b.length() > 0)
				b.append(" " + op + " ");
			else
				b.append("(");
			b.append(f.encode());
		}
		b.append(")");
		return b.toString();
	}

	@Override
	public String toString() {
		return getClass().getName() + " [filters=" + filters + ", op=" + op + "]";
	}
}
