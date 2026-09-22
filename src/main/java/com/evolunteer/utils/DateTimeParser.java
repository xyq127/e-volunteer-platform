package com.evolunteer.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateTimeParser {

    private static final String[] PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy"
    };

    private DateTimeParser() {
    }

    public static Date parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String value = text.trim();
        for (String pattern : PATTERNS) {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            format.setLenient(false);
            try {
                return format.parse(value);
            } catch (ParseException ignored) {

            }
        }
        throw new IllegalArgumentException("时间格式不正确：" + value + "，请按 yyyy-MM-dd HH:mm 填写");
    }
}
