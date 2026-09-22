package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeParserTest {

    @Test
    @DisplayName("支持浏览器日期时间控件与历史日期格式")
    void parseShouldSupportMultiplePatterns() throws Exception {
        assertEquals("2026-10-18 09:00:00", format(DateTimeParser.parse("2026-10-18T09:00")));
        assertEquals("2026-10-18 09:00:00", format(DateTimeParser.parse("2026-10-18 09:00")));
        assertEquals("2026-10-18 09:00:00", format(DateTimeParser.parse("2026-10-18T09:00:00")));
        assertEquals("2026-10-18 00:00:00", format(DateTimeParser.parse("2026-10-18")));
        assertEquals("2026-10-18 00:00:00", format(DateTimeParser.parse("10/18/2026")));
    }

    @Test
    @DisplayName("空白时间文本解析为空值，由存储过程校验必填")
    void parseShouldReturnNullForBlankText() {
        assertNull(DateTimeParser.parse(null));
        assertNull(DateTimeParser.parse(""));
        assertNull(DateTimeParser.parse("   "));
    }

    @Test
    @DisplayName("非法时间文本抛出可展示给用户的异常")
    void parseShouldRejectInvalidText() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> DateTimeParser.parse("2026年10月18日"));

        assertEquals(true, exception.getMessage().contains("时间格式不正确"));
        assertThrows(IllegalArgumentException.class, () -> DateTimeParser.parse("2026-13-40"));
    }

    private String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
