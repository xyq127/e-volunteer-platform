package com.evolunteer.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动时间解析工具类：志愿活动申报与重新申报页面提交的时间格式多样，
 * 统一由本工具解析为日期对象，解析失败时抛出异常由调用方转为页面提示。
 */
public final class DateTimeParser {

    /**
     * 支持的时间格式，按匹配优先级排列
     */
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

    /**
     * 解析页面提交的时间文本
     *
     * @param text 时间文本，支持 yyyy-MM-dd HH:mm、MM/dd/yyyy 等格式
     * @return 解析出的时间，文本为空时返回 null
     * @throws IllegalArgumentException 文本非空但不属于支持的格式时抛出
     */
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
                // 当前格式不匹配时继续尝试下一个格式
            }
        }
        throw new IllegalArgumentException("时间格式不正确：" + value + "，请按 yyyy-MM-dd HH:mm 填写");
    }
}
