package com.lonebytesoft.hamster.accounting.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class Utils {

    public static <K, V> Function<K, V> obtainNotFoundFunction(final String entity) {
        return id -> {
            throw new IllegalStateException("No " + entity + " id=" + id + " found");
        };
    }

    public static <T> BinaryOperator<T> obtainNoDuplicatesFunction() {
        return (u, v) -> {
            throw new IllegalStateException("Duplicate id: " + u);
        };
    }

    public static void setCalendarDayStart(final Calendar calendar) {
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
    }

    /**
     * Sets HTTP status and headers to response with redirection
     * @param response HTTP servlet response to update
     * @param location URL to redirect to
     */
    public static void httpRedirect(final HttpServletResponse response, final String location) {
        response.setHeader("Location", location);
        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
    }

    public static String getFullUrl(final HttpServletRequest request) {
        final String requestUri = request.getRequestURI();
        final String query = request.getQueryString();
        return query == null ? requestUri : requestUri + "?" + query;
    }

}
