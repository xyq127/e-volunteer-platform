package com.evolunteer.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 密码摘要工具类：使用 MD5 算法对登录密码进行摘要，注册与登录认证均使用该工具生成密文，
 * 系统中不保存也不打印密码明文。
 */
public class MD5Util {

    /**
     * 计算字符串的 MD5 摘要，结果固定为 32 位小写十六进制字符串
     *
     * @param password 待摘要的字符串
     * @return 32 位十六进制摘要
     */
    public static String encode(String password) {
        byte[] secretByte;
        try {
            secretByte = MessageDigest.getInstance("MD5").digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持 MD5 摘要算法", e);
        }

        StringBuilder md5Code = new StringBuilder(new BigInteger(1, secretByte).toString(16));
        while (md5Code.length() < 32) {
            md5Code.insert(0, "0");
        }
        return md5Code.toString();
    }
}
