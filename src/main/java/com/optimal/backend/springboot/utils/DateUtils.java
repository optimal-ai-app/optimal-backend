package com.optimal.backend.springboot.utils;

import java.sql.Timestamp;
import java.util.Date;

public class DateUtils {
    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getCurrentDatePlusDays(int days) {
        return new Date(getCurrentDate().getTime() + days * 24 * 60 * 60 * 1000);
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(getCurrentDate().getTime());
    }
}
