package com.identity4j.util.passwords;

/*
 * #%L
 * Identity4J Utils
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

    boolean isAdditionalAnalysis();
    
    float getMinStrength();
    
    boolean isContainUsername();
    
    Map<String, String> getAttributes();
}
