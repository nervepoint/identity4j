package com.identity4j.util.passwords;

import java.io.Serializable;
import java.util.Map;

public interface PasswordCharacteristics extends Serializable {

    float getVeryStrongFactor();

    int getMinimumSize();

    int getMaximumSize();
    
    /**
     * Get how many conditions must be true for the password to be considered OK. The conditions
     * include <i>Minimum Size</i>, <i>Minimum Upper Case</i>,
     * <i>Minimum Digits</i> and <i>Minimum Symbols</i>. 
     *  
     * @return minimum number of required matches for the all conditions
     */
    int getRequiredMatches();

    int getMinimumLowerCase();

    int getMinimumUpperCase();

    int getMinimumDigits();

    int getMinimumSymbols();
    
    int getHistorySize();

    /**
     * Get the list of characters that are considered <i>symbols</i>. If this password
     * characters is specific to a connector and <code>null</code> is returned, then
     * the default system wide list will be used.
     * 
     * @return symbols
     */
    char[] getSymbols();

    boolean isDictionaryWordsAllowed();
    
    boolean isContainUsername();
    
    Map<String, String> getAttributes();
}
