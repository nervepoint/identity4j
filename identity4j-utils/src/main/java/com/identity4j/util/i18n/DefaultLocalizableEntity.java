package com.identity4j.util.i18n;

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
