package com.identity4j.connector.unix;

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
