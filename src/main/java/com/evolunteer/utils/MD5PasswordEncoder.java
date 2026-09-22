package com.evolunteer.utils;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台密码编码器：登录认证时按平台约定的 MD5 摘要规则校验密码，志愿者组织与平台管理员共用。
 */
public class MD5PasswordEncoder implements PasswordEncoder {

    /**
     * 校验登录密码与数据库中的密码密文是否一致
     *
     * @param rawPassword     用户输入的密码明文
     * @param encodedPassword 数据库中保存的密码密文
     * @return 密码一致返回 true
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return MessageDigest.isEqual(
                encodedPassword.getBytes(StandardCharsets.UTF_8),
                MD5Util.encode(rawPassword.toString()).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对密码进行摘要
     *
     * @param rawPassword 密码明文
     * @return 32 位 MD5 密文
     */
    @Override
    public String encode(CharSequence rawPassword) {
        return MD5Util.encode(rawPassword.toString());
    }
}
