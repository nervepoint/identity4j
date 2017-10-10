/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

/*
 * #%L
 * Identity4J Active Directory JNDI
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