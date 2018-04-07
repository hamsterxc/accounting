package com.lonebytesoft.hamster.accounting.service.date;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.function.Consumer;

public class DateServiceImplTest {

    private DateService dateService = new DateServiceImpl();

    @Test
    public void testParseNull() {
        verify(null, calendar -> {});
    }

    @Test
    public void testParseEmpty() {
        verify("", calendar -> {});
    }

    @Test
    public void testParseDay() {
        verify("2", calendar -> {
            calendar.set(Calendar.DAY_OF_MONTH, 2);
        });
    }

    @Test
    public void testParseDayMonth() {
        verify("3.1", calendar -> {
            calendar.set(Calendar.DAY_OF_MONTH, 3);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
        });
    }

    @Test
    public void testParseDayMonthYearShort() {
        verify("3.1.99", calendar -> {
            calendar.set(Calendar.DAY_OF_MONTH, 3);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.YEAR, 1999);
        });
    }

    @Test
    public void testParseDayMonthYearLong() {
        verify("3.1.1999", calendar -> {
            calendar.set(Calendar.DAY_OF_MONTH, 3);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            calendar.set(Calendar.YEAR, 1999);
        });
    }

    @Test
    public void testParseRubbish() {
        verify("rubbish");
    }

    private void verify(final String input, final Consumer<Calendar> calendarModifier) {
        final Calendar calendar = dateService.obtainCalendar();
        calendarModifier.accept(calendar);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        final long expected = calendar.getTimeInMillis();

        final Long time = dateService.parse(input);
        Assert.assertNotNull(time);
        Assert.assertEquals(expected, time.longValue());
    }

    private void verify(final String input) {
        final Long time = dateService.parse(input);
        Assert.assertNull(time);
    }

}
