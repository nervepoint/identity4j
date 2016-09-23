package com.identity4j.connector.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.PropertyResourceBundle;

public class AttributeResourceBundle extends PropertyResourceBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	public AttributeResourceBundle(InputStream stream) throws IOException {
		super(stream);
	}

	public AttributeResourceBundle(Reader reader) throws IOException {
		super(reader);
	}

}
