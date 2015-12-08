package com.identity4j.connector.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.PropertyResourceBundle;

public class ScriptResourceBundle extends PropertyResourceBundle implements Serializable {

	private static final long serialVersionUID = 1L;

	public ScriptResourceBundle(InputStream stream) throws IOException {
		super(stream);
	}

	public ScriptResourceBundle(Reader reader) throws IOException {
		super(reader);
	}

}
