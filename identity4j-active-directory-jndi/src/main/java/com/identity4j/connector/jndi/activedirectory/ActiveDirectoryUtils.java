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

public class ActiveDirectoryUtils {

	public static String decodeSID(byte[] sid) {

		final StringBuilder strSid = new StringBuilder("S-");

		final int revision = sid[0];
		strSid.append(Integer.toString(revision));

		final int countSubAuths = sid[1] & 0xFF;

		long authority = 0;
		for (int i = 2; i <= 7; i++) {
			authority |= ((long) sid[i]) << (8 * (5 - (i - 2)));
		}
		strSid.append("-");
		strSid.append(Long.toHexString(authority));

		int offset = 8;
		int size = 4; // 4 bytes for each sub auth
		for (int j = 0; j < countSubAuths; j++) {
			long subAuthority = 0;
			for (int k = 0; k < size; k++) {
				subAuthority |= (long) (sid[offset + k] & 0xFF) << (8 * k);
			}

			strSid.append("-");
			strSid.append(subAuthority);

			offset += size;
		}

		return strSid.toString();
	}

	public static Long getRIDFromSID(byte[] sid) {
		String rid = "";
		for (int i = 6; i > 0; i--) {
			rid += byteToHex(sid[i]);
		}

		long authority = Long.parseLong(rid, 16);
		if (authority != 5) {
			return null;
		}

		rid = "";
		for (int j = 11; j > 7; j--) {
			rid += byteToHex(sid[j + 4 * 4]);
		}
		return new Long(Long.parseLong(rid, 16));
	}

	private static String byteToHex(byte b) {
		String ret = Integer.toHexString(b & 0xFF);
		if (ret.length() < 2) {
			ret = "0" + ret;
		}
		return ret;
	}
}
