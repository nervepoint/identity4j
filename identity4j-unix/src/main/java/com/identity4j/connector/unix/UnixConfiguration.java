package com.identity4j.connector.unix;

/*
 * #%L
 * Identity4J Unix
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

import com.identity4j.connector.flatfile.FlatFileConfiguration;
import com.identity4j.util.MultiMap;

public class UnixConfiguration extends FlatFileConfiguration {
    public static final String KEY_GROUP_FILE = "groupFileName";
    public static final String KEY_SHADOW_FILE = "shadowFileName";

    public UnixConfiguration(MultiMap configurationParameters) {
        super(configurationParameters);
        
        // Defaults
        if(!configurationParameters.containsKey("escapeCharacter")) {
        	configurationParameters.put("escapeCharacter", new String[] {"\\"});
        }
        if(!configurationParameters.containsKey("fieldSeparator")) {
        	configurationParameters.put("fieldSeparator", new String[] {":"});
        }
        if(!configurationParameters.containsKey("keyFieldIndex")) {
        	configurationParameters.put("keyFieldIndex", new String[] {"0"});
        }
        if(!configurationParameters.containsKey("passwordFieldIndex")) {
        	configurationParameters.put("passwordFieldIndex", new String[] {"1"});
        }
        if(!configurationParameters.containsKey("guidFieldIndex")) {
        	configurationParameters.put("guidFieldIndex", new String[] {"2"});
        }
        if(!configurationParameters.containsKey("fullNameFieldIndex")) {
        	configurationParameters.put("fullNameFieldIndex", new String[] {"4"});
        }
    }

    public String getGroupFileUri() {
        return getConfigurationParameters().getString(KEY_GROUP_FILE);
    }

    public String getShadowFileUri() {
        return getConfigurationParameters().getString(KEY_SHADOW_FILE);
    }
}
