package com.identity4j.util.unix;

public class UnixUtils {

	public static String escapeSingle(String str) {
		return str.replace("'", "\\'");
	}

	public static String escapeDouble(String str) {
		return str.replace("\"", "\\\"");
	}
}
