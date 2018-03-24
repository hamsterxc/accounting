package com.lonebytesoft.hamster.accounting.service.date;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Component
public class DateServiceImpl implements DateService {

    private static final List<DateParser> DATE_FORMATS = Arrays.asList(
            new DateParser("dd.MM.yy"),
            new DateParser("dd.MM.yyyy"),
            new DateParser("dd.MM.yyyy", "%s.yyyy"), // dd.MM
            new DateParser("dd.MM.yyyy", "%s.MM.yyyy"), // dd
            new DateParser("dd.MM.yyyy", "%sdd.MM.yyyy") // empty
    );

    @Override
    public long calculateDayStart(final long time, final int daysDelta) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        setCalendarDayStart(calendar);
        calendar.add(Calendar.DAY_OF_MONTH, daysDelta);
        return calendar.getTimeInMillis();
    }

    @Override
    public Long parse(String input) {
        return DATE_FORMATS
                .stream()
                .map(dateParser -> dateParser.parse(input))
                .filter(Objects::nonNull)
                .findFirst()
                .map(date -> {
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    setCalendarDayStart(calendar);
                    return calendar.getTimeInMillis();
                })
                .orElse(null);
    }

    private void setCalendarDayStart(final Calendar calendar) {
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
    }

}
