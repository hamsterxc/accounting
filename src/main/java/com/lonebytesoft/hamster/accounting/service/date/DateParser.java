package com.lonebytesoft.hamster.accounting.service.date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class DateParser {

    private static final Logger logger = LoggerFactory.getLogger(DateParser.class);

    private final ThreadLocal<DateFormat> dateFormat;
    private final String formatToParse;

    public DateParser(final String format) {
        this(format, null);
    }

    public DateParser(final String format, final String formatToParse) {
        this.dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(format));
        this.formatToParse = formatToParse;
    }

    public Date parse(final String input) {
        final String toParse;
        final String inputOrEmpty = input == null ? "" : input;
        if(formatToParse == null) {
            toParse = inputOrEmpty;
        } else {
            final String toFormat = String.format(formatToParse, inputOrEmpty);
            try {
                toParse = new SimpleDateFormat(toFormat).format(Calendar.getInstance().getTime());
            } catch (Exception e) {
                logger.trace("Could not parse input '{}'", input);
                return null;
            }
        }

        final ParsePosition parsePosition = new ParsePosition(0);
        final Date date = dateFormat.get().parse(toParse, parsePosition);
        if(parsePosition.getIndex() == toParse.length()) {
            return date;
        } else {
            logger.trace("Could not parse input '{}' (formatted '{}')", input, toParse);
            return null;
        }
    }

}
