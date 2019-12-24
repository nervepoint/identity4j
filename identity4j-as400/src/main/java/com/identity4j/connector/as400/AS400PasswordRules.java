/* HEADER */
package com.identity4j.connector.as400;

/*
 * #%L
 * Identity4J AS400
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


public enum AS400PasswordRules {
    QPWDEXPITV("Password expiration interval"),
    QPWDLVL("Password Level"), 
    QPWDMINLEN("Minimum Length"), 
    QPWDMAXLEN("Maximum Length"), 
    QPWDRQDDIF("Required Difference"), 
    QPWDLMTCHR("Restricted Characters"), 
    QPWDLMTAJC("Restricted Adjacent Characters"),
    QPWDLMTREP("Restrict Repeating Characters"),
    QPWDPOSDIF("Position Differnce"), 
    QPWDRQDDGT("Require Numeric Character"),
    QPWDEXPWRN("Password Expiration Warning Days");

    public final static String NO_PASSWORD_EXP = "*NOMAX";
    private String meaning;

    private AS400PasswordRules(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }
}
