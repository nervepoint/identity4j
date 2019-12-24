package com.identity4j.util.i18n;

/*
 * #%L
 * Identity4J Utils
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

public class DefaultLocalizableEntity implements LocalizableEntity {
    public String id;
    public String bundleName;
    

	public DefaultLocalizableEntity() {
	}

	public DefaultLocalizableEntity(String bundleName, String id) {
		super();
		this.id = id;
		this.bundleName = bundleName;
	}

	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}

	public String getBundleName() {
	    return bundleName;
	}

	public void setBundleName(String bundleName) {
	    this.bundleName = bundleName;
	}

}
