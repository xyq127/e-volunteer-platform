package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 密码摘要工具类单元测试：校验摘要结果、固定长度与空值处理。
 */
class MD5UtilTest {

    @Test
    @DisplayName("相同密码的摘要结果一致且为 32 位十六进制字符串")
    void encodeShouldReturnStableHexCode() {
        String encoded = MD5Util.encode("123456");

        assertEquals("e10adc3949ba59abbe56e057f20f883e", encoded);
        assertEquals(32, encoded.length());
        assertTrue(encoded.matches("[0-9a-f]{32}"));
        assertEquals(encoded, MD5Util.encode("123456"));
    }

    @Test
    @DisplayName("不同密码的摘要结果不同")
    void encodeShouldDifferForDifferentPasswords() {
        assertNotEquals(MD5Util.encode("123456"), MD5Util.encode("1234567"));
        assertNotEquals(MD5Util.encode("abc"), MD5Util.encode("中国志愿者"));
    }

    @Test
    @DisplayName("中文密码的摘要结果与字符集无关且长度固定")
    void encodeShouldSupportChinesePassword() {
        String encoded = MD5Util.encode("志愿者服务平台");

        assertEquals(32, encoded.length());
        assertEquals(encoded, MD5Util.encode("志愿者服务平台"));
    }

    @Test
    @DisplayName("空明文在计算摘要前由调用方校验")
    void encodeShouldRejectNullPassword() {
        assertThrows(NullPointerException.class, () -> MD5Util.encode(null));
    }
}
