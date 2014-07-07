package com.identity4j.connector.unix;

import com.identity4j.connector.flatfile.FlatFileConfiguration;
import com.identity4j.util.MultiMap;

public class UnixConfiguration extends FlatFileConfiguration {
    public static final String KEY_GROUP_FILE = "groupFileName";
    public static final String KEY_SHADOW_FILE = "shadowFileName";

    public UnixConfiguration(MultiMap configurationParameters) {
        super(configurationParameters);
    }

    public String getGroupFileUri() {
        return getConfigurationParameters().getString(KEY_GROUP_FILE);
    }

    public String getShadowFileUri() {
        return getConfigurationParameters().getString(KEY_SHADOW_FILE);
    }
}
