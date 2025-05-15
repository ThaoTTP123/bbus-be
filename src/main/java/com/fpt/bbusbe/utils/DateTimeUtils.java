package com.fpt.bbusbe.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {
    public static LocalDateTime convertToLocalDateTime(String dateTimeStr) {
        // Define the format of the date-time string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the string into LocalDateTime
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    public static Date convertStringToDate(String timeString, String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(s);
        return Date.from(LocalDateTime.parse(timeString, formatter).atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    public static String convertDateToString(Date date, String s) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(s);
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).format(formatter);
    }
}

