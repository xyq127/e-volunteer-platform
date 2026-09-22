package com.evolunteer.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

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
