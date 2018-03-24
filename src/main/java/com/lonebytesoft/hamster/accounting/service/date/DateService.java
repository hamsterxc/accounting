package com.lonebytesoft.hamster.accounting.service.date;

public interface DateService {

    Long parse(String input);

    long calculateDayStart(final long time, final int daysDelta);

}
