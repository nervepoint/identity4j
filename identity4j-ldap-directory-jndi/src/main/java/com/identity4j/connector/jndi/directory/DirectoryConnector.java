/* HEADER */
package com.identity4j.connector.jndi.directory;

import javax.naming.CommunicationException;
import javax.naming.NamingException;

import com.identity4j.connector.exception.ConnectorException;
import com.identity4j.connector.exception.InvalidLoginCredentialsException;
/*
 * #%L
 * Idenity4J LDAP Directory JNDI
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
public class DirectoryConnector extends AbstractDirectoryConnector<DirectoryConfiguration>  {

	@Override
	protected String processNamingException(NamingException nme) {
		if (nme instanceof CommunicationException) {
			return super.processNamingException(nme);
		}

		DirectoryExceptionParser dep = new DirectoryExceptionParser(nme);
		String message;
		switch (dep.getCode()) {
		case 0:
			message = "The LDAP operation completed successfully, but the server returned an exception.";
			break;
		case 1:
			message = "The LDAP server encountered an operations error.";
			break;
		case 2:
			message = "The LDAP request has a protocol error.";
			break;
		case 3:
			message = "The LDAP operation exceeded the server time limit.";
			break;
		case 4:
			message = "The LDAP operation exceeded the server size limit.";
			break;
		case 5:
			message = "The LDAP comparison evaluated to false.";
			break;
		case 6:
			message = "The LDAP comparison evaluated to true.";
			break;
		case 7:
			message = "The LDAP server does not support the requested authentication method.";
			break;
		case 8:
			message = "The LDAP server requires stronger authentication.";
			break;
		case 13:
			message = "The LDAP server requires confidentiality protection, such as TLS.";
			break;
		case 10:
			message = "The LDAP server returned a referral.";
			break;
		case 11:
			message = "The LDAP operation exceeded an administrative limit.";
			break;
		case 12:
			message = "The LDAP server does not support a required critical extension.";
			break;
		case 14:
			message = "The LDAP SASL bind is still in progress.";
			break;
		case 16:
			message = "The requested LDAP attribute does not exist.";
			break;
		case 17:
			message = "The LDAP attribute type is undefined.";
			break;
		case 18:
			message = "The LDAP matching rule is inappropriate for the attribute.";
			break;
		case 19:
			message = "The LDAP operation violates a directory constraint.";
			break;
		case 20:
			message = "The LDAP attribute or value already exists.";
			break;
		case 21:
			message = "The LDAP attribute value has invalid syntax.";
			break;
		case 32:
			message = "The requested LDAP object does not exist.";
			break;
		case 34:
			message = "The LDAP distinguished name has invalid syntax.";
			break;
		case 33:
			message = "The LDAP request encountered an alias problem.";
			break;
		case 36:
			message = "The LDAP server could not dereference the alias.";
			break;
		case 35:
			message = "The LDAP entry is a leaf and cannot contain subordinate entries.";
			break;
		case 48:
			message = "The LDAP authentication method is inappropriate for this operation.";
			break;
		case 49:
			throw new InvalidLoginCredentialsException();
		case 50:
			message = "Insufficient access rights to perform the LDAP operation.";
			break;
		case 51:
			message = "The LDAP server is busy.";
			break;
		case 52:
			message = "The LDAP service is unavailable.";
			break;
		case 81:
			message = "The LDAP server is down.";
			break;
		case 91:
			message = "The LDAP client could not connect to the server.";
			break;
		case 53:
			message = "The LDAP server is unwilling to perform the requested operation.";
			break;
		case 54:
			message = "The LDAP server detected a referral loop.";
			break;
		case 60:
			message = "The LDAP sort control is missing or invalid.";
			break;
		case 61:
			message = "The LDAP virtual-list offset is out of range.";
			break;
		case 76:
			message = "The LDAP virtual-list-view control reported an error.";
			break;
		case 64:
			message = "The LDAP operation violates a naming constraint.";
			break;
		case 65:
			message = "The LDAP entry violates its object class requirements.";
			break;
		case 66:
			message = "The LDAP operation is not allowed on a non-leaf entry.";
			break;
		case 67:
			message = "The LDAP operation is not allowed on the relative distinguished name.";
			break;
		case 68:
			message = "The LDAP entry already exists.";
			break;
		case 69:
			message = "The LDAP object class modification is not permitted.";
			break;
		case 70:
			message = "The LDAP operation returned results that are too large.";
			break;
		case 71:
			message = "The LDAP operation affects multiple directory service agents.";
			break;
		case 80:
			message = "The LDAP server returned an unspecified error.";
			break;
		case 82:
			message = "The LDAP client encountered a local error.";
			break;
		case 83:
			message = "The LDAP client encountered an encoding error.";
			break;
		case 84:
			message = "The LDAP client encountered a decoding error.";
			break;
		case 85:
			message = "The LDAP operation timed out.";
			break;
		case 86:
			message = "The LDAP authentication type is unknown.";
			break;
		case 87:
			message = "The LDAP search filter is invalid.";
			break;
		case 88:
			message = "The LDAP operation was canceled by the user.";
			break;
		case 118:
			message = "The LDAP operation was canceled.";
			break;
		case 89:
			message = "The LDAP request contains an invalid parameter.";
			break;
		case 90:
			message = "The LDAP client ran out of memory.";
			break;
		case 92:
			message = "The LDAP client or server does not support the requested operation.";
			break;
		case 93:
			message = "The requested LDAP control was not found.";
			break;
		case 94:
			message = "The LDAP server returned no results.";
			break;
		case 95:
			message = "The LDAP server has more results to return.";
			break;
		case 96:
			message = "The LDAP client detected a referral loop.";
			break;
		case 97:
			message = "The LDAP referral limit was exceeded.";
			break;
		case 100:
			message = "The LDAP server returned an invalid response.";
			break;
		case 101:
			message = "The LDAP server returned an ambiguous response.";
			break;
		case 112:
			message = "The LDAP server does not support TLS.";
			break;
		case 122:
			message = "The LDAP assertion condition failed.";
			break;
		case 123:
			message = "LDAP authorization was denied.";
			break;
		case 113:
			message = "The LDAP server returned an intermediate response.";
			break;
		case 114:
			message = "The LDAP response contains an unknown type.";
			break;
		case 119:
			message = "The LDAP operation does not exist.";
			break;
		case 120:
			message = "The LDAP operation could not be completed in time to cancel it.";
			break;
		case 121:
			message = "The LDAP operation cannot be canceled.";
			break;
		case 4096:
			message = "The LDAP synchronization refresh is required.";
			break;
		case 16654:
			message = "The LDAP request completed with no operation performed.";
			break;
		default:
			message = dep.getMessage();
			break;
		}

		throw new ConnectorException(message, nme);
	}
}