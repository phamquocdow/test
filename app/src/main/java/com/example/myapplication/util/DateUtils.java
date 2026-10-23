package com.example.myapplication.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class DateUtils {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private DateUtils() {
    }

    public static String today() {
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(new Date());
    }

    public static String now() {
        return new SimpleDateFormat(DATE_TIME_PATTERN, Locale.US).format(new Date());
    }

    public static String daysFromToday(int days) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, days);
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(c.getTime());
    }

    /** month is 0-based, as returned by DatePickerDialog. */
    public static String format(int year, int month, int day) {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
    }

    /** Returns {year, month (0-based), day} of the given date, or of today if it can't be parsed. */
    public static int[] toYmd(String date) {
        Calendar c = Calendar.getInstance();
        Date parsed = parse(date);
        if (parsed != null) {
            c.setTime(parsed);
        }
        return new int[]{c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)};
    }

    /** A fruit is expired once its expiry date is before today. */
    public static boolean isExpired(String expiryDate) {
        return expiryDate != null && expiryDate.compareTo(today()) < 0;
    }

    /** Days from today until the given date (negative if it is in the past). */
    public static long daysUntil(String date) {
        Date target = parse(date);
        Date today = parse(today());
        if (target == null || today == null) {
            return 0;
        }
        return Math.round((target.getTime() - today.getTime()) / (double) TimeUnit.DAYS.toMillis(1));
    }

    public static boolean isValidDate(String date) {
        return parse(date) != null;
    }

    private static Date parse(String date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat f = new SimpleDateFormat(DATE_PATTERN, Locale.US);
        f.setLenient(false);
        try {
            return f.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
