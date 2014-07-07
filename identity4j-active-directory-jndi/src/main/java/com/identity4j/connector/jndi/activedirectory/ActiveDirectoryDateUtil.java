/* HEADER */
package com.identity4j.connector.jndi.activedirectory;

import java.util.Calendar;
import java.util.Date;

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
        return (int) (timeStamp / -86400L / 10000000L);
    }

	public static long javaDataToADTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        
        Calendar calendar2 = Calendar.getInstance();
        calendar.clear();
        calendar.set(1601, 0, 1, 0, 0);
        
        return ( calendar.getTimeInMillis() - calendar2.getTimeInMillis() ) * 10000;        
	}
}