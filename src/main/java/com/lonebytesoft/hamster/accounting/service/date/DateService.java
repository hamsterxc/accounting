package com.lonebytesoft.hamster.accounting.service.date;

import java.util.Calendar;

public interface DateService {

    Calendar obtainCalendar();

    Long parse(String input);

    long calculateDayStart(final long time, final int daysDelta);

}
