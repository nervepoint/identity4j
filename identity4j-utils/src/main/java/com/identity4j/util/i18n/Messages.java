/* HEADER */
package com.identity4j.util.i18n;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for localising string resources. The standard place for storing
 * message bundle resources is in the <i>i18n</i> folder. <br/>
 * Any method that requires a bundle name will automatically prepend
 * <code>/i18n</code> to acquire the full class loader resource to search for.
 */
public class Messages {

	private Messages() {
		// don't create an instance
	}

	/**
	 * Returns message
	 * 
	 * @param bundle bundle name
	 * @param key key
	 * @param arguments message replacement arguments
	 * @return localised text
	 */
	public static String getString(String bundle, String key, Object... arguments) {
		return getString(null, Locale.getDefault(), bundle, key, arguments);
	}

	/**
	 * Returns message
	 * 
	 * @param bundle bundle name
	 * @param key key
	 * @param arguments message replacement arguments
	 * @return localised text
	 */
	public static String getString(String bundle, Locale locale, String key, Object... arguments) {
		return getString(null, locale, bundle, key, arguments);
	}

	/**
	 * Returns message
	 * 
	 * @param bundle bundle name
	 * @param key key
	 * @param arguments message replacement arguments
	 * @return localised text
	 */
	public static String getString(ClassLoader classLoader, String bundle, String key, Object... arguments) {
		return getString(classLoader, Locale.getDefault(), bundle, key, arguments);
	}

	/**
	 * Returns message
	 * 
	 * @param locale local (default is used if <code>null</code>)
	 * @param bundle bundle name
	 * @param key key
	 * @param arguments message replacement arguments
	 * @return localised text
	 */
	public static String getString(ClassLoader classLoader, Locale locale, String bundle, String key, Object... arguments) {
		try {
			ResourceBundle resource = getBundle(classLoader, locale, bundle);
			String localizedString = resource.getString(key);
			if (arguments == null || arguments.length == 0) {
				return localizedString;
			}
			
			localizedString = escapeQuotes(localizedString);

			MessageFormat messageFormat = new MessageFormat(localizedString);
			messageFormat.setLocale(locale);
			return messageFormat.format(formatParameters(arguments));
		} catch (MissingResourceException mre) {
			return "??" + (bundle == null ? "" : bundle + ":") + '!' + key + '!';
		}
	}

	public static ResourceBundle getBundle(Locale locale, String bundle) {
		return getBundle(null, locale, bundle);
	}

	public static ResourceBundle getBundle(ClassLoader classLoader, Locale locale, String bundle) {
		if (bundle == null) {
			return null;
		}
		if (!bundle.startsWith("i18n/")) {
			bundle = "i18n/" + bundle;
		}
		classLoader = determineClassLoader(classLoader);
		return ResourceBundle.getBundle(bundle, locale, classLoader);

	}

	protected static ClassLoader determineClassLoader(ClassLoader classLoader) {
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		if (classLoader == null) {
			classLoader = Messages.class.getClassLoader();
		}
		return classLoader;
	}

	public static boolean isResourceExists(Locale locale, String bundle, String key, ClassLoader classLoader) {
		try {
			ResourceBundle resource = getBundle(classLoader, locale, bundle);
			resource.getString(key);
			return true;
		} catch (MissingResourceException mre) {
			return false;
		}
	}

	private static Object[] formatParameters(Object... arguments) {
		Collection<Object> formatted = new ArrayList<Object>(arguments.length);
		for (Object arg : arguments) {
			if (arg instanceof Date) {
				formatted.add(DateFormat.getDateTimeInstance().format(arg));
			} else {
				formatted.add(arg);
			}
		}
		return formatted.toArray(new Object[formatted.size()]);
	}

	private static String escapeQuotes(String content) {
		if (content == null) {
			return null;
		}
		StringBuilder bui = new StringBuilder();
		char lastC = ' ';
		for (char c : content.toCharArray()) {
			if (lastC > -1 && c == '\'' && lastC != '\'') {
				bui.append("\'");
			}
			bui.append(c);
			lastC = c;
		}
		return bui.toString();
	}
}