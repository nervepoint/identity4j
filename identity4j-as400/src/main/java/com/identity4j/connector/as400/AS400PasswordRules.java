/* HEADER */
package com.identity4j.connector.as400;

public enum AS400PasswordRules {
    QPWDEXPITV("Password expiration interval"), QPWDLVL("Password Level"), QPWDMINLEN("Minimum Length"), QPWDMAXLEN(
                    "Maximum Length"), QPWDRQDDIF("Required Difference"), QPWDLMTCHR("Restricted Characters"), QPWDLMTAJC(
                    "Restricted Adjacent Characters"), QPWDLMTREP("Restrict Repeating Characters"), QPWDPOSDIF("Position Differnce"), QPWDRQDDGT(
                    "Require Numeric Character");

    public final static String NO_PASSWORD_EXP = "*NOMAX";
    private String meaning;

    private AS400PasswordRules(String meaning) {
        this.meaning = meaning;
    }

    public String getMeaning() {
        return meaning;
    }
}
