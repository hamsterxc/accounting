package com.lonebytesoft.hamster.accounting.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateParser {

    private static final Logger logger = LoggerFactory.getLogger(DateParser.class);

    private final ThreadLocal<DateFormat> dateFormat;
    private final String formatToParse;

    public DateParser(final String format) {
        this(format, "%s");
    }

    public DateParser(final String format, final String formatToParse) {
        this.dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
        this.formatToParse = formatToParse;
    }

    public Long parse(final String input) {
        if((input == null) || (input.length() == 0)) {
            return null;
        }

        final String toParse = String.format(formatToParse, input);
        final ParsePosition parsePosition = new ParsePosition(0);
        final Date date = dateFormat.get().parse(toParse, parsePosition);
        if(parsePosition.getIndex() == toParse.length()) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Utils.setCalendarDayStart(calendar);
            return calendar.getTimeInMillis();
        } else {
            logger.trace("Could not parse input '{}' (formatted '{}')", input, toParse);
            return null;
        }
    }

}
