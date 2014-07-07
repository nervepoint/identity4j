/* HEADER */
package com.identity4j.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Provides general purpose String utilities.
 */
public final class StringUtil {

	private StringUtil() {
		// don't create an instance
	}

	/**
	 * Test if a string is <code>null</code> if it is an empty string when
	 * trimmed.
	 * 
	 * @param value
	 * @return null or trimmed blank string
	 */
	public static boolean isNullOrEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}

	/**
	 * Return an empty string when <code>null</code>, otherwise return the
	 * string.
	 * 
	 * @param value
	 * @return string or empty string when <code>null</code>
	 */
	public static String nonNull(String value) {
		return value == null ? "" : value;
	}

	/**
	 * Return an empty string when <code>null</code>, otherwise return the
	 * string.
	 * 
	 * @param value
	 * @return string or empty string when <code>null</code>
	 */
	public static String trim(String value) {
		return value == null ? "" : value.trim();
	}

	/**
	 * Returns the string within this string of the first occurrence before the
	 * <strong>first</strong> specified substring match e.g. supplying something@another.com would
	 * return something when a match of @ was supplied. If the match is not
	 * found the original supplied value is returned.
	 * 
	 * @param value
	 * @param match
	 * @return the string before the supplied match or the original string if no
	 *         match is found
	 */
	public static String getBefore(String value, String match) {
		int indexOf = value == null ? -1 : value.indexOf(match);
		if (indexOf == -1 || indexOf == value.length()) {
			return nonNull(value);
		}
		return value.substring(0, indexOf);
	}

	/**
	 * Returns the string within this string of the first occurrence before the
	 * <strong>last</strong> specified substring match e.g. supplying something@another.com would
	 * return something when a match of @ was supplied. If the match is not
	 * found the original supplied value is returned.
	 * 
	 * @param value
	 * @param match
	 * @return the string before the supplied match or the original string if no
	 *         match is found
	 */
	public static String getBeforeLast(String value, String match) {
		int indexOf = value == null ? -1 : value.lastIndexOf(match);
		if (indexOf == -1 || indexOf == value.length()) {
			return nonNull(value);
		}
		return value.substring(0, indexOf);
	}
	
	/**
	 * Returns the string within this string of the first occurrence after the <strong>last</strong>
	 * specified substring match e.g. supplying something@another.com would
	 * return another.com when a match of @ was supplied. If the match is not
	 * found the original supplied value is returned.
	 * 
	 * @param value
	 * @param match
	 * @return the string after the supplied match or the original string if no
	 *         match is found
	 */
	public static String getAfterLast(String value, String match) {
		int indexOf = value == null ? -1 : value.lastIndexOf(match);
		if (indexOf == -1 || indexOf == value.length()) {
			return nonNull(value);
		}
		return value.substring(indexOf + match.length(), value.length());
	}

	/**
	 * Returns the string within this string of the first occurrence after the <strong>first</strong>
	 * specified substring match e.g. supplying something@another.com would
	 * return another.com when a match of @ was supplied. If the match is not
	 * found the original supplied value is returned.
	 * 
	 * @param value
	 * @param match
	 * @return the string after the supplied match or the original string if no
	 *         match is found
	 */
	public static String getAfter(String value, String match) {
		int indexOf = value == null ? -1 : value.indexOf(match);
		if (indexOf == -1 || indexOf == value.length()) {
			return nonNull(value);
		}
		return value.substring(indexOf + match.length(), value.length());
	}

	/**
	 * Tokenize the supplied value, splitting it when the ! delimiter is
	 * encountered.
	 * 
	 * Note that if the <tt>value</tt> is null, it does not throw an exception
	 * and instead returns an empty <tt>Array</tt>.
	 * 
	 * @param value a string to be parsed
	 * @return the array built from the delimited value
	 */
	public static String[] toDefaultArray(String value) {
		List<String> list = toDefaultList(value);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Tokenize the supplied value, splitting it when the supplied delimiter is
	 * encountered.
	 * 
	 * Note that if the <tt>value</tt> is null, it does not throw an exception
	 * and instead returns an empty <tt>Collection</tt>.
	 * 
	 * @param value a string to be parsed
	 * @param delimiter the delimiter
	 * @return the collection built from the delimited value
	 */
	public static String[] toArray(String value, char delimiter) {
		List<String> list = toList(value, delimiter);
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Tokenize the supplied value, splitting it when the ! delimiter is
	 * encountered.
	 * 
	 * Note that if the <tt>value</tt> is null, it does not throw an exception
	 * and instead returns an empty <tt>Collection</tt>.
	 * 
	 * @param value a string to be parsed
	 * @return the collection built from the delimited value
	 */
	public static List<String> toDefaultList(String value) {
		return toList(value, '!');
	}

	/**
	 * Tokenize the supplied value, splitting it when the supplied delimiter is
	 * encountered.
	 * 
	 * Note that if the <tt>value</tt> is null, it does not throw an exception
	 * and instead returns an empty <tt>Collection</tt>.
	 * 
	 * @param value a string to be parsed
	 * @param delimiter the delimiter
	 * @return the collection built from the delimited value
	 */
	public static List<String> toList(String value, char delimiter) {
		if (value == null) {
			return Collections.emptyList();
		}

		List<String> values = new ArrayList<String>();
		boolean escape = false;
		StringBuilder word = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (ch == '\\' && !escape) {
				escape = true;
			} else if (!escape && ch == delimiter) {
				values.add(word.toString());
				word.setLength(0);
			} else {
				if (escape && (ch == 'n')) {
					word.append('\n');
				} else if (escape && (ch == 'r')) {
					word.append('\r');
				} else if (escape && (ch == 't')) {
					word.append('\t');
				} else {
					word.append(ch);
				}
				escape = false;
			}
		}
		if (values.size() > 0 || word.length() > 0) {
			values.add(word.toString());
		}
		return values;
	}

	public static String toDefaultString(String... values) {
		return toString("!", values);
	}

	public static String toString(String delimiter, String... values) {
		if (values == null) {
			return "";
		}
		return toString(Arrays.asList(values), delimiter);
	}

	/**
	 * Convert a collection of string values into a bang (!) delimited string.
	 * 
	 * @param values
	 * @return bang separated string
	 */
	public static String toDefaultString(Collection<String> values) {
		return toString(values, "!");
	}

	/**
	 * Convert a collection of string values into a delimited string.
	 * 
	 * @param values
	 * @param delimiter delimiter string
	 * @return delimited string
	 */
	public static String toString(Collection<String> values, String delimiter) {
		if (values == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		int delim = 0;
		for (String value : values) {
			if (builder.length() > 0) {
				builder.append(delimiter);
			}
			for (char ch : value.toCharArray()) {
				if (delim < delimiter.length() && ch == delimiter.charAt(delim)) {
					delim++;
					if (delim == delimiter.length()) {
						builder.append('\\');
						builder.append(delimiter);
						delim = 0;
					}
				} else if (ch == '\n') {
					builder.append("\\n");
				} else if (ch == '\r') {
					builder.append("\\r");
				} else if (ch == '\t') {
					builder.append("\\t");
				} else if (ch == '\\') {
					builder.append("\\\\");
				} else {
					builder.append(ch);
				}
			}
		}

		return builder.toString();
	}

	/**
	 * Compare a collection of strings to an array of strings.
	 */
	public static boolean equals(Collection<String> asCollection, String[] values) {
		final List<String> asList = Arrays.asList(values);
		final boolean containsAll = asCollection.containsAll(asList);
		final int size = asCollection.size();
		final int length = values.length;
		return containsAll && length == size;
	}

	public final static String convertByteToString(byte[] objectGuid) {
		StringBuilder builder = new StringBuilder();
		for (byte element : objectGuid) {
			int item = element & 0xFF;
			if (item <= 0xF) {
				builder.append("0");
			}
			builder.append(Integer.toHexString(item));
		}
		return builder.toString();
	}

	/**
	 * Convert a space separated string to upper camel case.
	 * 
	 * @param value value
	 * @return upper camel cased string
	 */
	public static String upperCamelCase(String value) {
		StringBuilder builder = new StringBuilder();
		for (StringTokenizer tokenizer = new StringTokenizer(value, " .", true); tokenizer.hasMoreTokens();) {
			String token = tokenizer.nextToken();
			if (!token.equals(" ")) {
				if (token.equals(".")) {
					builder.append(".");
				} else {
					builder.append(Character.toUpperCase(token.charAt(0)));
					if (token.length() > 1) {
						builder.append(token.substring(1));
					}
				}
			}
		}
		return builder.toString();
	}

	public static String getURLFilename(URL url) {
		String name = url.getPath();
		int idx = name.lastIndexOf('/');
		if (idx != -1) {
			name = name.substring(idx + 1);
		}
		return name;
	}

	public static String getLastToken(String string, String lastToken) {
		int idx = string.lastIndexOf(lastToken);
		if (idx == -1) {
			return string;
		}
		return string.substring(idx + lastToken.length());
	}

	/**
	 * Compact a classpath string by replacing any occurence of the current
	 * working directory with '.'. If any of the remaining paths contain the
	 * user homes directory, that will be replaced with a '~'.
	 * 
	 * @param text text
	 * @return replaced text
	 */
	public static String compactClasspathString(String classpathString) {
		return classpathString.replace(System.getProperty("user.dir"), ".").replace(System.getProperty("user.home"), "~");
	}

	public static boolean isArrayNullOrEmpty(String[] values) {
		return values == null || values.length == 0;
	}

	public static boolean isNullOrEmptyOrFirstElementBlank(String[] values) {
		return isArrayNullOrEmpty(values) || values[0].equals("");
	}

	public static String getValue(String[] values) {
		return values == null || values.length == 0 ? null : values[0];
	}

	public static String trimToLength(String text, int length) {
		return text.length() > length ? text.substring(0, length) + " ...." : text;
	}

	public static String getFirstItemOrNull(List<String> items) {
		return items == null || items.size() == 0 ? null : items.get(0);
	}

	public static String capitalisedToKey(String name) {
		boolean capNext = true;
		StringBuilder bui = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (Character.isLetter(c)) {
				if (capNext) {
					bui.append(Character.toUpperCase(c));
					capNext = false;
				} else {
					bui.append(c);
				}
			} else {
				if (Character.isDigit(c)) {
					bui.append(c);
				} else {
					capNext = true;
				}
			}
		}
		return bui.toString();
	}
	
	/**
	 * Splits a String separated by a delimiter into tokens and returns them in a List.
	 * 
	 * @param value
	 * @param delimiter
	 * @return list containing tokens
	 */
	public static List<String> toList(String value,String delimiter){
		List<String> collection = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(value, delimiter);
		while (tokenizer.hasMoreElements()) {
			collection.add(tokenizer.nextToken());
		}
		return collection;
	}
	
}