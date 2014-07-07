package com.identity4j.util.i18n;

@SuppressWarnings("serial")
public class LocalizableRuntimeException extends RuntimeException implements LocalizableEntity {

	private String bundle;

	public LocalizableRuntimeException() {
	}

	public LocalizableRuntimeException(String bundle, String key) {
		super(key);
		this.bundle = bundle;
	}

	@Override
	public String getId() {
		return getMessage();
	}

	@Override
	public String getBundleName() {
		return bundle;
	}


}
