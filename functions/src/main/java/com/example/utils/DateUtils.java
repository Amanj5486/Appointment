package com.example.appointment.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtils {

    /**
     * Converts a given Date to epoch time in milliseconds.
     *
     * @param date Date to be converted
     * @return epoch time in milliseconds
     */
    public static long dateToEpoch(Date date) {
        return date.toInstant().toEpochMilli();
    }

    /**
     * Converts a given LocalDateTime to epoch time in milliseconds.
     *
     * @param dateTime LocalDateTime to be converted
     * @return epoch time in milliseconds
     */
    public static long dateTimeToEpoch(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli();
    }
}

