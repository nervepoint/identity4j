package com.identity4j.connector;

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