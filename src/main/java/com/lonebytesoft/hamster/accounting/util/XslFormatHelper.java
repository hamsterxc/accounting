package com.lonebytesoft.hamster.accounting.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum XslFormatHelper {

    INSTANCE;

    private final Map<String, DateFormat> dateFormatCache = new HashMap<>();

    public String formatDate(final String timestamp, final String format) {
        final DateFormat dateFormat = dateFormatCache.computeIfAbsent(format, SimpleDateFormat::new);

        final long time = Long.parseLong(timestamp);
        final Date date = new Date(time);

        return dateFormat.format(date);
    }

    public static XslFormatHelper getInstance() {
        return INSTANCE;
    }

}
