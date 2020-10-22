package com.identity4j.connector.unix;

import com.identity4j.connector.Connector;
import com.identity4j.connector.flatfile.AbstractFlatFileConfiguration;
import com.identity4j.util.MultiMap;

public class UnixConfiguration extends AbstractFlatFileConfiguration {
    public static final String KEY_GROUP_FILE = "groupFileName";
    public static final String KEY_SHADOW_FILE = "shadowFileName";

    public UnixConfiguration() {
        this(new MultiMap());
    }
    
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
        if(!configurationParameters.containsKey("charset")) {
        	configurationParameters.put("charset", new String[] {"UTF-8"});
        }
        if(!configurationParameters.containsKey("fileName")) {
        	configurationParameters.put("fileName", new String[] {"file:///etc/passwd"});
        }
        if(!configurationParameters.containsKey("groupFileName")) {
        	configurationParameters.put("groupFileName", new String[] {"file:///etc/group"});
        }
        if(!configurationParameters.containsKey("shadowFileName")) {
        	configurationParameters.put("shadowFileName", new String[] {"file:///etc/shadow"});
        }
        if(!configurationParameters.containsKey("includedUsers")) {
        	configurationParameters.put("includedUsers", new String[] {"root"});
        }
        if(!configurationParameters.containsKey("allowPasswordReset")) {
        	configurationParameters.put("allowPasswordReset", new String[] {"true"});
        }
        if(!configurationParameters.containsKey("allowPasswordChange")) {
        	configurationParameters.put("allowPasswordChange", new String[] {"true"});
        }
    }

    public String getGroupFileUri() {
        return getConfigurationParameters().getString(KEY_GROUP_FILE);
    }

    public String getShadowFileUri() {
        return getConfigurationParameters().getString(KEY_SHADOW_FILE);
    }

	@Override
	public Class<? extends Connector<?>> getConnectorClass() {
		return UnixConnector.class;
	}
}
