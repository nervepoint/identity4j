package com.identity4j.connector;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Media implements IMedia, Serializable {
	
	screen, email, mobile, emailTemplate;
	
	public final static List<Media> NONE = Collections.emptyList();
	public final static List<Media> ALL = null;
	final static List<Media> HAVE_TYPE = Arrays.asList(Media.email, Media.emailTemplate);
	final static List<Media> HAVE_SUBJECT = Arrays.asList(Media.email, Media.emailTemplate);

	public boolean isForNotification() {
		switch (this) {
		case email:
		case mobile:
			return true;
		default:
			return false;
		}
	}

	public boolean hasSubject() {
		return HAVE_SUBJECT.contains(this);
	}

	public boolean hasType() {
		return HAVE_TYPE.contains(this);
	}
}