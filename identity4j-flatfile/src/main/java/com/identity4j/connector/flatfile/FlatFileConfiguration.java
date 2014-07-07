/* HEADER */
package com.identity4j.connector.flatfile;

import com.identity4j.connector.vfs.AbstractVFSConfiguration;
import com.identity4j.util.MultiMap;
import com.identity4j.util.StringUtil;

public class FlatFileConfiguration extends AbstractVFSConfiguration {
    public static final String KEY_FILENAME = "fileName";
    public static final String KEY_FIELD_SEPARATOR = "fieldSeparator";
    public static final String KEY_CHARSET = "charset";
    public static final String KEY_ESCAPE_CHARACTER = "escapeCharacter";
    public static final String KEY_KEY_FIELD_INDEX = "keyFieldIndex";
    public static final String KEY_GUID_FIELD_INDEX = "guidFieldIndex";
    public static final String KEY_FULL_NAME_FIELD_INDEX = "fullNameFieldIndex";
    public static final String KEY_PASSWORD_FIELD_INDEX = "passwordFieldIndex";
    public static final String KEY_IDENTITY_PASSWORD_ENCODING = "identityPasswordEncoding";

    public FlatFileConfiguration(MultiMap configurationParameters) {
        super(configurationParameters);
    }

    /**
     * The type of password encoding used for this directory.
     * 
     * @return password encoding type
     */
    public final String getIdentityPasswordEncoding() {
        return getConfigurationParameters().getStringOrFail(KEY_IDENTITY_PASSWORD_ENCODING);
    }

    public char getEscapeCharacter() {
        String val = getConfigurationParameters().getString(KEY_ESCAPE_CHARACTER);
        return StringUtil.isNullOrEmpty(val) ? (char) -1 : val.charAt(0);
    }

    public char getFieldSeparator() {
        String val = getConfigurationParameters().getString(KEY_FIELD_SEPARATOR);
        return StringUtil.isNullOrEmpty(val) ? ',' : val.charAt(0);
    }

    public String getCharset() {
        return getConfigurationParameters().getStringOrDefault(KEY_CHARSET, "UTF-8");
    }

    public int getKeyFieldIndex() {
        return getConfigurationParameters().getIntegerOrDefault(KEY_KEY_FIELD_INDEX, 0);
    }

    public int getGuidFieldIndex() {
        return getConfigurationParameters().getIntegerOrDefault(KEY_GUID_FIELD_INDEX, 0);
    }

    public int getFullNameFieldIndex() {
        return getConfigurationParameters().getIntegerOrDefault(KEY_FULL_NAME_FIELD_INDEX, 0);
    }

    public int getPasswordFieldIndex() {
        return getConfigurationParameters().getIntegerOrDefault(KEY_PASSWORD_FIELD_INDEX, 0);
    }

    @Override
    public String getUri() {
        return getConfigurationParameters().getString(KEY_FILENAME);
    }
}