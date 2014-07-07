/* HEADER */
package com.identity4j.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides general purpose utilites.
 */
public final class Util {

	private final static Log LOG = LogFactory.getLog(Util.class);

	private Util() {
		// don't create an instance
	}

	/**
	 * <p>
	 * Checks if two dates are on the same day ignoring time.
	 * </p>
	 * 
	 * @param date1 the first date, not altered, not null
	 * @param date2 the second date, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException if either date is <code>null</code>
	 */
	public static boolean isSameDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isSameDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if two calendars represent the same day ignoring time.
	 * </p>
	 * 
	 * @param cal1 the first calendar, not altered, not null
	 * @param cal2 the second calendar, not altered, not null
	 * @return true if they represent the same day
	 * @throws IllegalArgumentException if either calendar is <code>null</code>
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
			.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	}

	/**
	 * <p>
	 * Checks if a date is today.
	 * </p>
	 * 
	 * @param date the date, not altered, not null.
	 * @return true if the date is today.
	 * @throws IllegalArgumentException if the date is <code>null</code>
	 */
	public static boolean isToday(Date date) {
		return isSameDay(date, Calendar.getInstance().getTime());
	}

	/**
	 * <p>
	 * Checks if a calendar date is today.
	 * </p>
	 * 
	 * @param cal the calendar, not altered, not null
	 * @return true if cal date is today
	 * @throws IllegalArgumentException if the calendar is <code>null</code>
	 */
	public static boolean isToday(Calendar cal) {
		return isSameDay(cal, Calendar.getInstance());
	}

	/**
	 * <p>
	 * Checks if the first date is before the second date ignoring time.
	 * </p>
	 * 
	 * @param date1 the first date, not altered, not null
	 * @param date2 the second date, not altered, not null
	 * @return true if the first date day is before the second date day.
	 * @throws IllegalArgumentException if the date is <code>null</code>
	 */
	public static boolean isBeforeDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isBeforeDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if the first calendar date is before the second calendar date
	 * ignoring time.
	 * </p>
	 * 
	 * @param cal1 the first calendar, not altered, not null.
	 * @param cal2 the second calendar, not altered, not null.
	 * @return true if cal1 date is before cal2 date ignoring time.
	 * @throws IllegalArgumentException if either of the calendars are
	 *             <code>null</code>
	 */
	public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA))
			return true;
		if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA))
			return false;
		if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR))
			return true;
		if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR))
			return false;
		return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * <p>
	 * Checks if the first date is after the second date ignoring time.
	 * </p>
	 * 
	 * @param date1 the first date, not altered, not null
	 * @param date2 the second date, not altered, not null
	 * @return true if the first date day is after the second date day.
	 * @throws IllegalArgumentException if the date is <code>null</code>
	 */
	public static boolean isAfterDay(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		return isAfterDay(cal1, cal2);
	}

	/**
	 * <p>
	 * Checks if the first calendar date is after the second calendar date
	 * ignoring time.
	 * </p>
	 * 
	 * @param cal1 the first calendar, not altered, not null.
	 * @param cal2 the second calendar, not altered, not null.
	 * @return true if cal1 date is after cal2 date ignoring time.
	 * @throws IllegalArgumentException if either of the calendars are
	 *             <code>null</code>
	 */
	public static boolean isAfterDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			throw new IllegalArgumentException("The dates must not be null");
		}
		if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA))
			return false;
		if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA))
			return true;
		if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR))
			return false;
		if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR))
			return true;
		return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * <p>
	 * Checks if a date is after today and within a number of days in the
	 * future.
	 * </p>
	 * 
	 * @param date the date to check, not altered, not null.
	 * @param days the number of days.
	 * @return true if the date day is after today and within days in the future
	 *         .
	 * @throws IllegalArgumentException if the date is <code>null</code>
	 */
	public static boolean isWithinDaysFuture(Date date, int days) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return isWithinDaysFuture(cal, days);
	}

	/**
	 * <p>
	 * Checks if a calendar date is after today and within a number of days in
	 * the future.
	 * </p>
	 * 
	 * @param cal the calendar, not altered, not null
	 * @param days the number of days.
	 * @return true if the calendar date day is after today and within days in
	 *         the future .
	 * @throws IllegalArgumentException if the calendar is <code>null</code>
	 */
	public static boolean isWithinDaysFuture(Calendar cal, int days) {
		if (cal == null) {
			throw new IllegalArgumentException("The date must not be null");
		}
		Calendar today = Calendar.getInstance();
		Calendar future = Calendar.getInstance();
		future.add(Calendar.DAY_OF_YEAR, days);
		return (isAfterDay(cal, today) && !isAfterDay(cal, future));
	}

	/** Returns the given date with the time set to the start of the day. */
	public static Date getStart(Date date) {
		return clearTime(date);
	}

	/** Returns the given date with the time values cleared. */
	public static Date clearTime(Date date) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	/**
	 * Determines whether or not a date has any time values (hour, minute,
	 * seconds or millisecondsReturns the given date with the time values
	 * cleared.
	 */

	/**
	 * Determines whether or not a date has any time values.
	 * 
	 * @param date The date.
	 * @return true iff the date is not null and any of the date's hour, minute,
	 *         seconds or millisecond values are greater than zero.
	 */
	public static boolean hasTime(Date date) {
		if (date == null) {
			return false;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if (c.get(Calendar.HOUR_OF_DAY) > 0) {
			return true;
		}
		if (c.get(Calendar.MINUTE) > 0) {
			return true;
		}
		if (c.get(Calendar.SECOND) > 0) {
			return true;
		}
		if (c.get(Calendar.MILLISECOND) > 0) {
			return true;
		}
		return false;
	}

	/** Returns the given date with time set to the end of the day */
	public static Date getEnd(Date date) {
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 59);
		c.set(Calendar.MILLISECOND, 999);
		return c.getTime();
	}

	/**
	 * Returns the maximum of two dates. A null date is treated as being less
	 * than any non-null date.
	 */
	public static Date max(Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return null;
		if (d1 == null)
			return d2;
		if (d2 == null)
			return d1;
		return (d1.after(d2)) ? d1 : d2;
	}

	/**
	 * Returns the minimum of two dates. A null date is treated as being greater
	 * than any non-null date.
	 */
	public static Date min(Date d1, Date d2) {
		if (d1 == null && d2 == null)
			return null;
		if (d1 == null)
			return d2;
		if (d2 == null)
			return d1;
		return (d1.before(d2)) ? d1 : d2;
	}

	/** The maximum date possible. */
	public static Date MAX_DATE = new Date(Long.MAX_VALUE);

	public static <T> List<T> toList(Iterator<T> it) {
		List<T> l = new ArrayList<T>();
		while (it.hasNext()) {
			l.add(it.next());
		}
		return l;
	}

	public static int countIterator(Iterator<?> it) {
		int x = 0;
		while (it.hasNext()) {
			it.next();
			x++;
		}
		return x;
	}

	/**
	 * Prints an exceptions trace to a string.
	 * 
	 * @param t exception
	 * @return trace
	 */
	public static String traceString(Throwable t) {
		StringWriter s = new StringWriter();
		t.printStackTrace(new PrintWriter(s));
		return s.toString();
	}

	/**
	 * Parses the language / country code in a local object
	 * 
	 * @param languageCountry language country
	 * @return locale
	 */
	public static Locale parseLocale(String languageCountry) {
		String[] els = languageCountry.split("_");
		if (els.length == 2) {
			return new Locale(els[0], els[1]);
		} else if (els.length == 3) {
			return new Locale(els[0], els[1], els[2]);
		}
		return new Locale(els[0]);
	}

	/**
	 * Calculates if the current time is after the date supplied + the number of
	 * days.
	 * 
	 * @param fromDate
	 * @param days
	 * @return
	 */
	public static boolean isDatePast(Date fromDate, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fromDate);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		return new Date().after(calendar.getTime());
	}

	/**
	 * Calculates the date in the future by adding the supplied days to the
	 * current date.
	 * 
	 * @param days
	 * @return date
	 */
	public static Date futureDate(long days) {
		return new Date(futureTime(days));
	}

	/**
	 * Calculates the time in the future by adding the supplied days to the
	 * current time.
	 * 
	 * @param days
	 * @return time
	 */
	public static long futureTime(long days) {
		return System.currentTimeMillis() + daysToMillis(days);
	}

	/**
	 * Convert a number of days to milliseconds
	 * 
	 * @param days days
	 * @return milliseconds
	 */
	public static long daysToMillis(long days) {
		return days * 24l * 60l * 60l * 1000l;
	}

	/**
	 * Asserts that the supplied value is not null.
	 * 
	 * @param toCheck the value to check
	 * @param name the name of the supplied value
	 * @throws IllegalArgumentException if the supplied value is null.
	 */
	public static void assertNotNull(Object toCheck, String name) {
		if (toCheck == null) {
			throw new IllegalArgumentException(name + " must be set.");
		}
	}

	/**
	 * Return a very simple string represent of an object, assuming the object
	 * uses bean like getters (getXXX and isXXX). Only looks for zero argument
	 * methods and catches exceptions to replace with the text "!Exception!".
	 * 
	 * @param object object
	 * @return string
	 */
	public static String fromObject(Object object) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (Method m : object.getClass().getMethods()) {
			if ((m.getName().matches("get[A-Z]?.*") || m.getName().matches("is[A-Z]?.*")) && m.getParameterTypes().length == 0) {
				if (builder.length() > 1) {
					builder.append(", ");
				}
				String name = m.getName().startsWith("is") ? m.getName().substring(2) : m.getName().substring(3);
				builder.append(name);
				builder.append("=");
				try {
					builder.append(m.invoke(object, new Object[0]));
				} catch (Exception e) {
					builder.append("!Exception!");
				}
			}
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Remove precision from a calendar so that it represents just a day. A new
	 * instance is returned
	 * 
	 * @param calendar calendar
	 * @return new cal
	 */
	public static Calendar dayPrecision(Calendar calendar) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(calendar.getTime());
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		return cal;
	}

	/**
	 * Temporary debugging. References to this method should be checked before
	 * release.
	 * 
	 * @param message message pattern
	 * @param args message arguments
	 */
	public static void removeMe(String message, String... args) {
		System.out.println("*** REMOVEME **** " + MessageFormat.format(message, (Object[]) args));
	}

	public static Throwable getRootCause(Throwable exception, Class<? extends Throwable> clazz) {
		Throwable cause = exception;
		Throwable root = null;
		do {
			if (cause != null) {
				if (clazz == null || (clazz != null && clazz.isAssignableFrom(cause.getClass()))) {
					root = cause;
				}
			}
			cause = cause.getCause();
		} while (cause != null);
		return root;
	}

	/**
	 * Sleep for a short random amount of time
	 * 
	 * @param ms max time
	 */
	public static void randomSleep(long ms) {
		try {
			Thread.sleep((long) (ms * Math.random()));
		} catch (InterruptedException e) {
		}
	}

	public static int unzip(File zipfile, File directory) throws IOException {
		ZipFile zfile = new ZipFile(zipfile);
		int files = 0;
		Enumeration<? extends ZipEntry> entries = zfile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			File file = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				file.mkdirs();
			} else {
				file.getParentFile().mkdirs();
				InputStream in = zfile.getInputStream(entry);
				try {
					FileOutputStream fos = new FileOutputStream(file);
					try {
						IOUtils.copy(in, fos);
						LOG.info("Extracted " + file);
						files++;
					} finally {
						fos.close();
					}
				} finally {
					in.close();
				}
			}
		}
		return files;
	}

	public static void zip(File directory, File zipfile) throws IOException {
		URI base = directory.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directory);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = null;
		try {
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directory = queue.pop();
				for (File kid : directory.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						FileInputStream fin = new FileInputStream(kid);
						try {
							IOUtils.copy(fin, zout);
						} finally {
							fin.close();
						}
						zout.closeEntry();
					}
				}
			}
		} finally {
			if(res!=null) {
				res.close();
			}
			out.close();
		}
	}

	public static boolean differs(Object o1, Object o2) {
		if (o1 == null && o2 != null || o2 == null && o1 != null) {
			return true;
		}
		if (o1 instanceof Date) {
			Date d1 = (Date) o1;
			Date d2 = (Date) o2;
			o1 = new Date((d1.getTime() / 1000) * 1000);
			o2 = new Date((d2.getTime() / 1000) * 1000);
		}
		return o1 != null && !o1.equals(o2);
	}
}