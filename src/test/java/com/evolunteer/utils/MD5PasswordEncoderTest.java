package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台密码编码器单元测试：校验密码加密与登录密码比对规则。
 */
class MD5PasswordEncoderTest {

    private final MD5PasswordEncoder encoder = new MD5PasswordEncoder();

    @Test
    @DisplayName("密码加密结果与平台约定的 MD5 摘要一致")
    void encodeShouldUsePlatformMd5Rule() {
        assertEquals("e10adc3949ba59abbe56e057f20f883e", encoder.encode("123456"));
    }

    @Test
    @DisplayName("密码正确时校验通过")
    void matchesShouldAcceptCorrectPassword() {
        assertTrue(encoder.matches("123456", "e10adc3949ba59abbe56e057f20f883e"));
    }

    @Test
    @DisplayName("密码错误时校验不通过")
    void matchesShouldRejectWrongPassword() {
        assertFalse(encoder.matches("1234567", "e10adc3949ba59abbe56e057f20f883e"));
        assertFalse(encoder.matches("", "e10adc3949ba59abbe56e057f20f883e"));
    }

    @Test
    @DisplayName("密码为空时校验不通过而不抛出异常")
    void matchesShouldRejectEmptyInput() {
        assertFalse(encoder.matches(null, "e10adc3949ba59abbe56e057f20f883e"));
        assertFalse(encoder.matches("123456", null));
    }
}
