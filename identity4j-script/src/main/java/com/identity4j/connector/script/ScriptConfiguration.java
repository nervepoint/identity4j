/* HEADER */
package com.identity4j.connector.script;

/*
 * #%L
 * Identity4J Scripted Connectors
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

import java.io.IOException;

import com.identity4j.connector.AbstractConnectorConfiguration;
import com.identity4j.util.IOUtil;
import com.identity4j.util.MultiMap;

public class ScriptConfiguration extends AbstractConnectorConfiguration {

	public static final String KEY_SCRIPT_CONTENT = "script.content";
	public static final String KEY_SCRIPT_MIME_TYPE = "script.mimeType";

	public ScriptConfiguration(MultiMap configurationParameters) {
		super(configurationParameters);
	}

	/**
	 * Get the script content.
	 * 
	 * @return script content
	 */
	public String getScriptContent() throws IOException {
		return getContent(KEY_SCRIPT_CONTENT);
	}

	protected String getContent(String key) throws IOException {
		String content = configurationParameters.getStringOrFail(key);
		if (content.startsWith("// res://")) {
			return IOUtil.getStringFromResource(this.getClass(), content.substring(3));
		}
		return content;
	}

	/**
	 * Convenience method to get the script resource name.
	 * 
	 * @return script resource name
	 */
	public String getScriptResource() {
		String content = configurationParameters.getStringOrDefault(KEY_SCRIPT_CONTENT, "");
		if (content.equals("")) {
			return "TestScript";
		}
		if (content.startsWith("// res://")) {
			return content.substring(3);
		}
		return "NoScript";
	}

	/**
	 * Get the mime type of the script. This will determine the script
	 * interpreter to use.
	 * 
	 * @return mime type of script
	 */
	public String getScriptMimeType() {
		return configurationParameters.getStringOrFail(KEY_SCRIPT_MIME_TYPE);
	}

	@Override
	public String getUsernameHint() {
		return null;
	}

	@Override
	public String getHostnameHint() {
		return null;
	}
}