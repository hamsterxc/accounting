package com.lonebytesoft.hamster.accounting.util;

import java.util.Calendar;

public final class Utils {

    public static void setCalendarDayStart(final Calendar calendar) {
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
    }

    public static long calculateDayStart(final long time) {
        return calculateDayStart(time, 0);
    }

    public static long calculateDayStart(final long time, final int daysDelta) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        setCalendarDayStart(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, daysDelta);
        return calendar.getTimeInMillis();
    }

}
