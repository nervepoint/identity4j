/**
 * Copyright (c) SafeStone Ltd.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information
 * of SafeStone Ltd. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * with SafeStone Ltd.
 */

package com.identity4j.connector.activedirectory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class encapsulates the user control behaviour. For more information
 * http://support.microsoft.com/kb/305144.
 */
public final class UserAccountControl {
    /** The logon script will be run. */
    public static final int SCRIPT_FLAG = 0x0001;
    /** The user account is disabled. */
    public static final int ACCOUNTDISABLE_FLAG = 0x0002;
    /** The home folder is required. */
    public static final int HOMEDIR_REQUIRED_FLAG = 0x0008;
    /** The home folder is required. */
    // doesn't work
    public static final int LOCKOUT_FLAG = 0x0010;
    /** No password is required. */
    public static final int PASSWD_NOTREQD_FLAG = 0x0020;
    /** The user can send an encrypted password. */
    public static final int ENCRYPTED_TEXT_PWD_ALLOWED_FLAG = 0x0080;
    /**
     * This is an account for users whose primary account is in another domain.
     * This account provides user access to this domain, but not to any domain
     * that trusts this domain. This is sometimes referred to as a local user
     * account.
     */
    // doesn't work
    public static final int TEMP_DUPLICATE_ACCOUNT_FLAG = 0x0100;
    /** This is a default account type that represents a typical user. */
    public static final int NORMAL_ACCOUNT_FLAG = 0x0200;
    /**
     * This is a permit to trust an account for a system domain that trusts
     * other domains.
     */
    // doesn't work
    public static final int INTERDOMAIN_TRUST_ACCOUNT_FLAG = 0x0800;
    /**
     * This is a computer account for a computer that is running Microsoft
     * Windows NT 4.0 Workstation, Microsoft Windows NT 4.0 Server, Microsoft
     * Windows 2000 Professional, or Windows 2000 Server and is a member of this
     * domain.
     */
    // doesn't work
    public static final int WORKSTATION_TRUST_ACCOUNT_FLAG = 0x1000;
    /**
     * This is a computer account for a domain controller that is a member of
     * this domain.
     */
    // doesn't work
    public static final int SERVER_TRUST_ACCOUNT_FLAG = 0x2000;
    /** Represents the password, which should never expire on the account. */
    public static final int DONT_EXPIRE_PASSWORD_FLAG = 0x10000;
    /** This is an MNS logon account. */
    public static final int MNS_LOGON_ACCOUNT_FLAG = 0x20000;
    /**
     * When this flag is set, it forces the user to log on by using a smart
     * card.
     */
    public static final int SMARTCARD_REQUIRED_FLAG = 0x40000;
    /**
     * The service account (the user or computer account) under which a service
     * runs is trusted for Kerberos delegation.
     */
    public static final int TRUSTED_FOR_DELEGATION_FLAG = 0x80000;
    /**
     * The security context of the user is not delegated to a service even if
     * the service account is set as trusted for Kerberos delegation.
     */
    public static final int NOT_DELEGATED_FLAG = 0x100000;
    /**
     * Restrict this principal to use only Data Encryption Standard (DES)
     * encryption types for keys.
     */
    public static final int USE_DES_KEY_ONLY_FLAG = 0x200000;
    /**
     * This account does not require Kerberos pre-authentication for logging on.
     */
    public static final int DONT_REQ_PREAUTH_FLAG = 0x400000;
    /** The user's password has expired. */
    public static final int PASSWORD_EXPIRED_FLAG = 0x800000;
    /**
     * The account is enabled for delegation. This is a security-sensitive
     * setting. Accounts with this option enabled should be tightly controlled.
     * This setting allows a service that runs under the account to assume a
     * client's identity and authenticate as that user to other remote servers
     * on the network.
     */
    public static final int TRUSTED_TO_AUTH_FOR_DELEGATION_FLAG = 0x1000000;

    public static final String SCRIPT_ATTRIBUTE = "RunScript";
    public static final String ACCOUNTDISABLE_ATTRIBUTE = "AccountDisabled";
    public static final String HOMEDIR_REQUIRED_ATTRIBUTE = "HomeDirectoryRequired";

    public static final String LOCKOUT_ATTRIBUTE = "LockOut";
    public static final String PASSWD_NOTREQD_ATTRIBUTE = "ChangePasswordAtNextLogon";
    public static final String ENCRYPTED_TEXT_PWD_ALLOWED_ATTRIBUTE = "PasswordUseingReversableEncryption";
    // doesn't work
    public static final String TEMP_DUPLICATE_ACCOUNT_ATTRIBUTE = "TempDuplicateAccount";
    // doesn't work
    public static final String INTERDOMAIN_TRUST_ACCOUNT_ATTRIBUTE = "InterdomainTrustAccount";
    // doesn't work
    public static final String WORKSTATION_TRUST_ACCOUNT_ATTRIBUTE = "WorkstationTrustAccount";
    // doesn't work
    public static final String SERVER_TRUST_ACCOUNT_ATTRIBUTE = "ServerTrustAccount";
    public static final String DONT_EXPIRE_PASSWORD_ATTRIBUTE = "PasswordNeverExpires";
    public static final String MNS_LOGON_ACCOUNT_ATTRIBUTE = "MNSLogonAccount";
    public static final String SMARTCARD_REQUIRED_ATTRIBUTE = "SmartCardIsRequiredForInteractiveLogon";
    public static final String TRUSTED_FOR_DELEGATION_ATTRIBUTE = "AccountIsTrustedForDelegation";
    public static final String NOT_DELEGATED_ATTRIBUTE = "AccountIsSensitiveAndCannotBeDelegated";
    public static final String USE_DES_KEY_ONLY_ATTRIBUTE = "UseDESEncryptionTypesForThisAccount";
    public static final String DONT_REQ_PREAUTH_ATTRIBUTE = "DoNotRequireKerberosPreauthentication";
    public static final String PASSWORD_EXPIRED_ATTRIBUTE = "PasswordExpired";
    public static final String TRUSTED_TO_AUTH_FOR_DELEGATION_ATTRIBUTE = "EnabledForDelegation";
    public static final String NORMAL_ACCOUNT_ATTRIBUTE = "NormalAccount";

    private static final Map<String, Integer> CONTROL_FLAGS = new HashMap<String, Integer>();

    static {
        CONTROL_FLAGS.put(SCRIPT_ATTRIBUTE, SCRIPT_FLAG);
        CONTROL_FLAGS.put(ACCOUNTDISABLE_ATTRIBUTE, ACCOUNTDISABLE_FLAG);
        CONTROL_FLAGS.put(HOMEDIR_REQUIRED_ATTRIBUTE, HOMEDIR_REQUIRED_FLAG);
        // doesn't work
        CONTROL_FLAGS.put(LOCKOUT_ATTRIBUTE, LOCKOUT_FLAG);
        CONTROL_FLAGS.put(PASSWD_NOTREQD_ATTRIBUTE, PASSWD_NOTREQD_FLAG);
        CONTROL_FLAGS.put(ENCRYPTED_TEXT_PWD_ALLOWED_ATTRIBUTE, ENCRYPTED_TEXT_PWD_ALLOWED_FLAG);
        // doesn't work
        CONTROL_FLAGS.put(TEMP_DUPLICATE_ACCOUNT_ATTRIBUTE, TEMP_DUPLICATE_ACCOUNT_FLAG);
        // doesn't work
        CONTROL_FLAGS.put(INTERDOMAIN_TRUST_ACCOUNT_ATTRIBUTE, INTERDOMAIN_TRUST_ACCOUNT_FLAG);
        // doesn't work
        CONTROL_FLAGS.put(WORKSTATION_TRUST_ACCOUNT_ATTRIBUTE, WORKSTATION_TRUST_ACCOUNT_FLAG);
        // doesn't work
        CONTROL_FLAGS.put(SERVER_TRUST_ACCOUNT_ATTRIBUTE, SERVER_TRUST_ACCOUNT_FLAG);
        CONTROL_FLAGS.put(DONT_EXPIRE_PASSWORD_ATTRIBUTE, DONT_EXPIRE_PASSWORD_FLAG);
        CONTROL_FLAGS.put(MNS_LOGON_ACCOUNT_ATTRIBUTE, MNS_LOGON_ACCOUNT_FLAG);
        CONTROL_FLAGS.put(SMARTCARD_REQUIRED_ATTRIBUTE, SMARTCARD_REQUIRED_FLAG);
        CONTROL_FLAGS.put(TRUSTED_FOR_DELEGATION_ATTRIBUTE, TRUSTED_FOR_DELEGATION_FLAG);
        CONTROL_FLAGS.put(NOT_DELEGATED_ATTRIBUTE, NOT_DELEGATED_FLAG);
        CONTROL_FLAGS.put(NORMAL_ACCOUNT_ATTRIBUTE, NORMAL_ACCOUNT_FLAG);
        CONTROL_FLAGS.put(USE_DES_KEY_ONLY_ATTRIBUTE, USE_DES_KEY_ONLY_FLAG);
        CONTROL_FLAGS.put(DONT_REQ_PREAUTH_ATTRIBUTE, DONT_REQ_PREAUTH_FLAG);
        CONTROL_FLAGS.put(PASSWORD_EXPIRED_ATTRIBUTE, PASSWORD_EXPIRED_FLAG);
        CONTROL_FLAGS.put(TRUSTED_TO_AUTH_FOR_DELEGATION_ATTRIBUTE, TRUSTED_TO_AUTH_FOR_DELEGATION_FLAG);
    }

    private UserAccountControl() {
    }
    public static boolean isPasswordNeverExpire(int userAccountControl) {
        return isValueSet(userAccountControl, DONT_EXPIRE_PASSWORD_FLAG);
    }

    public static boolean isPasswordChangePermitted(int userAccountControl) {
        // this was supposed to check cannotChangePassword as well, but it
        // appears that Active
        // directory sets this in a completely different way which can't be
        // access from JNDI
        // boolean cannotChangePassword = isValueSet(userAccountControl,
        // PASSWD_CANT_CHANGE);
//        return !cannotChangePassword;
    	return true;
    }

    /**
     * Calculate the user control value for the given user status and
     * parameters.
     * 
     * @param controlFlag the current control flag setting
     * @param enabled the status of the user
     * @param flagSettings the settings for all of the user control flags
     * @return the user control flag
     */
    public static int getUserAccountControlFlag(int controlFlag, Boolean enabled, Map<String, Boolean> flagSettings) {
        for (Map.Entry<String, Boolean> element : flagSettings.entrySet()) {
            int flagValue = CONTROL_FLAGS.get(element.getKey());
            if (element.getValue()) {
                controlFlag = controlFlag | flagValue;
            } else {
                controlFlag = controlFlag & ~flagValue;
            }
        }

        if (enabled != null) {
            if (enabled) {
                controlFlag = controlFlag & ~ACCOUNTDISABLE_FLAG;
            } else {
                controlFlag = controlFlag | ACCOUNTDISABLE_FLAG;
            }
        }
        return controlFlag;
    }

    /**
     * Finds if each of the control flags have been enabled for a user.
     * 
     * @param accountControlFlag the users account control setting
     * @return a <code>java.util.Map</code> with the flag name ->
     *         <code>java.lang.Boolean</code>
     */
    public static Map<String, Boolean> getAccountControlSettings(int accountControlFlag) {
        Map<String, Boolean> settings = new HashMap<String, Boolean>();
        for (Map.Entry<String, Integer> element : CONTROL_FLAGS.entrySet()) {
            String key = element.getKey();
            int flagValue = element.getValue();
            settings.put(key, isValueSet(accountControlFlag, flagValue));
        }
        return settings;
    }

    /**
     * Verifies if the flag has been enabled on the user.
     * 
     * @param userAccountControl the user control flag
     * @param valueToTest the flag to verify
     * @return true if the valueToTest has been set for the user
     */
    public static boolean isValueSet(int userAccountControl, int valueToTest) {
        return (userAccountControl | valueToTest) == userAccountControl;
    }
}