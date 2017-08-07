package com.lonebytesoft.hamster.accounting.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DateParserTest {

    private static final List<DateParser> PARSERS = Arrays.asList(
            new DateParser("dd.MM.yyyy", "%s." + Calendar.getInstance().get(Calendar.YEAR)), // dd.MM
            new DateParser("dd.MM.yy"),
            new DateParser("dd.MM.yyyy")
    );

    @Test
    public void testDayMonth() {
        final String input = "31.12";

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        Utils.setCalendarDayStart(calendar);
        final Long expected = calendar.getTimeInMillis();

        Assert.assertEquals(expected, PARSERS.get(0).parse(input));
        Assert.assertNull(PARSERS.get(1).parse(input));
        Assert.assertNull(PARSERS.get(2).parse(input));
    }

    @Test
    public void testDayMonthYearShort() {
        final String input = "31.12.99";

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.YEAR, 1999);
        Utils.setCalendarDayStart(calendar);
        final Long expected = calendar.getTimeInMillis();

        Assert.assertNull(PARSERS.get(0).parse(input));
        Assert.assertEquals(expected, PARSERS.get(1).parse(input));
//        Assert.assertNull(PARSERS.get(2).parse(input));
    }

    @Test
    public void testDayMonthYear() {
        final String input = "31.12.2012";

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.YEAR, 2012);
        Utils.setCalendarDayStart(calendar);
        final Long expected = calendar.getTimeInMillis();

        Assert.assertNull(PARSERS.get(0).parse(input));
        Assert.assertEquals(expected, PARSERS.get(1).parse(input));
        Assert.assertEquals(expected, PARSERS.get(2).parse(input));
    }

    @Test
    public void testRubbish() {
        final String input = "rubbish";

        Assert.assertNull(PARSERS.get(0).parse(input));
        Assert.assertNull(PARSERS.get(1).parse(input));
        Assert.assertNull(PARSERS.get(2).parse(input));
    }

}
