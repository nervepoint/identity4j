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


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class ActiveDirectoryDateUtil {

    private ActiveDirectoryDateUtil() {
    }

    /**
     * Converts an Active Directory long value into a
     * <code>java.util.Date</code>.
     * 
     * @param timeStamp the time to convert
     * @return the <code>java.util.Date</code> representing the long
     */
    public static Date adTimeToJavaDate(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(1601, 0, 1, 0, 0);
        timeStamp = timeStamp / 10000 + calendar.getTime().getTime();
        return new Date(timeStamp);
    }

    /**
     * Converts an Active Directory long value into a number of days.
     * 
     * @param timeStamp the time to convert
     * @return days representing the long
     */
    public static int adTimeToJavaDays(long timeStamp) {
        return (int) timeStamp == -9223372036854775808L ? 0 : (int) (timeStamp / -86400L / 10000000L);
    }

	public static long javaDataToADTime(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        calendar.setTime(date);
        
        Calendar calendar2 = Calendar.getInstance();
        calendar.clear();
        calendar.set(1601, 0, 1, 0, 0);
        
        return ( calendar.getTimeInMillis() - calendar2.getTimeInMillis() ) * 10000;        
	}
}