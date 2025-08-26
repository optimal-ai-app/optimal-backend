package com.optimal.backend.springboot.utils;

import java.sql.Timestamp;
import java.sql.Date;
import java.util.Calendar;

public class DateUtils {
    public static Date getCurrentDate() {
        return new Date(0);
    }

    public static Date getCurrentDatePlusDays(int days) {
        return new Date(getCurrentDate().getTime() + days * 24 * 60 * 60 * 1000);
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(getCurrentDate().getTime());
    }

    public static double getExecutionTimeInSeconds(long startTime) {
        return (double)(System.nanoTime() - startTime) / 1000000000;
    }

    //Gets the dates for current sunday to previous monday
    public static ToFromDate getDates() {

        Calendar today = Calendar.getInstance();
        today.setTime(new java.util.Date());

        // Find latest Sunday (today if today is Sunday)
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
        Calendar endDateCal = (Calendar) today.clone();
        endDateCal.add(Calendar.DATE, Calendar.SUNDAY - dayOfWeek);

        // Start date is previous Monday (6 days before latest Sunday)
        Calendar startDateCal = (Calendar) endDateCal.clone();
        startDateCal.add(Calendar.DATE, -6);

        Date startDate = new Date(startDateCal.getTimeInMillis());
        Date endDate = new Date(endDateCal.getTimeInMillis());

        return new ToFromDate(startDate, endDate);
    }

    public static class ToFromDate {
        public final Date startDate;
        public final Date endDate;

        public ToFromDate(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    
}
