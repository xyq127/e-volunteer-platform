package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 随机初始密码工具类单元测试：校验长度、字符集与随机性。
 */
class RandomPasswordUtilTest {

    @Test
    @DisplayName("生成的随机密码长度为 8 且只包含允许的字符")
    void randomPasswordShouldMatchAllowedCharacters() {
        for (int i = 0; i < 20; i++) {
            String password = RandomPasswordUtil.randomPassword();

            assertEquals(8, password.length());
            assertTrue(password.matches("[2-9A-HJ-NP-Za-km-z]+"), password);
            assertFalse(password.contains("0"));
            assertFalse(password.contains("O"));
            assertFalse(password.contains("1"));
            assertFalse(password.contains("l"));
        }
    }

    @Test
    @DisplayName("连续生成的密码不相同")
    void randomPasswordShouldBeRandom() {
        assertNotEquals(RandomPasswordUtil.randomPassword(), RandomPasswordUtil.randomPassword());
    }
}
