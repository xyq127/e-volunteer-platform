package com.evolunteer.utils;

import java.security.SecureRandom;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 随机初始密码工具类：平台管理员为组织账号设置初始密码、为忘记密码的账号重置密码时使用，
 * 生成的密码由大小写字母与数字组成，并剔除了容易混淆的字符。
 */
public final class RandomPasswordUtil {

    /**
     * 密码字符集合，去掉了容易混淆的 0、O、1、I、l
     */
    private static final char[] PASSWORD_CHARACTERS =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz".toCharArray();

    /**
     * 随机密码长度
     */
    private static final int PASSWORD_LENGTH = 8;

    private RandomPasswordUtil() {
    }

    /**
     * 生成随机初始密码
     *
     * @return 8 位随机密码
     */
    public static String randomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARACTERS[random.nextInt(PASSWORD_CHARACTERS.length)]);
        }
        return password.toString();
    }
}
