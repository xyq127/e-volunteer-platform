package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurePasswordEncoderTest {

    private final SecurePasswordEncoder passwordEncoder = new SecurePasswordEncoder();

    @Test
    @DisplayName("加盐摘要可以校验原始密码且密文格式带有算法与迭代次数")
    void encodeShouldProduceVerifiableCipher() {
        String encoded = passwordEncoder.encode("123456");

        assertTrue(encoded.startsWith("pbkdf2$120000$"));
        assertTrue(passwordEncoder.matches("123456", encoded));
        assertFalse(passwordEncoder.matches("1234567", encoded));
        assertFalse(passwordEncoder.matches("123456", encoded + "x"));
        assertFalse(passwordEncoder.matches(null, encoded));
        assertFalse(passwordEncoder.matches("123456", null));
    }

    @Test
    @DisplayName("同一密码两次加密得到不同密文，避免撞库时批量识破相同密码")
    void encodeShouldUseRandomSalt() {
        String first = passwordEncoder.encode("123456");
        String second = passwordEncoder.encode("123456");

        assertNotEquals(first, second);
        assertTrue(passwordEncoder.matches("123456", first));
        assertTrue(passwordEncoder.matches("123456", second));
    }

    @Test
    @DisplayName("历史 MD5 密文可以校验通过并需要进行升级")
    void matchesShouldSupportLegacyMd5Cipher() {
        String legacy = MD5Util.encode("123456");

        assertTrue(passwordEncoder.matches("123456", legacy));
        assertFalse(passwordEncoder.matches("654321", legacy));
        assertTrue(passwordEncoder.upgradeEncoding(legacy));
    }

    @Test
    @DisplayName("加盐密文无需再次升级")
    void upgradeEncodingShouldSkipSaltedCipher() {
        assertFalse(passwordEncoder.upgradeEncoding(passwordEncoder.encode("123456")));
        assertFalse(passwordEncoder.upgradeEncoding(null));
    }

    @Test
    @DisplayName("非法密文校验返回 false 而不抛出异常")
    void matchesShouldRejectMalformedCipher() {
        assertFalse(passwordEncoder.matches("123456", "pbkdf2$abc$notbase64$notbase64"));
        assertFalse(passwordEncoder.matches("123456", "unknown$format"));
        assertEquals("123456", "123456");
    }
}
