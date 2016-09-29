package com.identity4j.util.http;

public class HttpUtil {

	public static String concatenateUriParts(String part1, String part2) {
		while(part1.endsWith("/")) {
			part1 = part1.substring(0, part1.length() - 1);
		}
		while(part2 != null && part2.startsWith("/")) {
			part2 = part2.substring(1);
		}
		return part1 + (part2 == null ? "" : "/" + part2);
	}
}
