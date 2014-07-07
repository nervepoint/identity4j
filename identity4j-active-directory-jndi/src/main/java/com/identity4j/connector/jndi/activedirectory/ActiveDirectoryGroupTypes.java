/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

/**
 * This class encapsulates the group type behaviour. For more information
 * http://msdn2.microsoft.com/en-us/library/ms675935.aspx
 */
interface ActiveDirectoryGroupTypes {
    /** Specifies a group that is created by the system. */
    int SYSTEM_GROUP = 0x00000001;
    /** Specifies a group with global scope. */
    int GROUP_WITH_GLOBAL_SCOPE = 0x00000002;
    /** Specifies a group with domain local scope. */
    int GROUP_WITH_DOMAIN_LOCAL_SCOPE = 0x00000004;
    /** Specifies a group with universal scope. */
    int GROUP_WITH_UNIVERSAL_SCOPE = 0x00000008;
    /** Specifies an APP_BASIC group for Windows Server Authorization Manager. */
    int APP_BASIC_GROUP = 0x00000010;
    /** Specifies an APP_QUERY group for Windows Server Authorization Manager. */
    int APP_QUERY_GROUP = 0x00000020;
    /**
     * Specifies a security group. If this flag is not set, then the group is a
     * distribution group.
     */
    int SECURITY_GROUP = 0x80000000;
}