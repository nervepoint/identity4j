package com.identity4j.util.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

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

public class HttpUtil {

	public static String encode(String part) {
		try {
			return URLEncoder.encode(part, "UTF-8");
		} catch (UnsupportedEncodingException uo) {
			throw new RuntimeException("No encoding.", uo);
		}
	}

	public static String decode(String part) {
		try {
			return URLDecoder.decode(part, "UTF-8");
		} catch (UnsupportedEncodingException uo) {
			throw new RuntimeException("No encoding.", uo);
		}
	}

	public static String concatenateUriParts(String part1, String part2) {
		while (part1.endsWith("/")) {
			part1 = part1.substring(0, part1.length() - 1);
		}
		while (part2 != null && part2.startsWith("/")) {
			part2 = part2.substring(1);
		}
		return part1 + (part2 == null ? "" : "/" + part2);
	}
}
