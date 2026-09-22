package com.evolunteer.utils;

import java.security.SecureRandom;

public final class RandomPasswordUtil {

    private static final char[] PASSWORD_CHARACTERS =
            "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz".toCharArray();

    private static final int PASSWORD_LENGTH = 8;

    private RandomPasswordUtil() {
    }

    public static String randomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARACTERS[random.nextInt(PASSWORD_CHARACTERS.length)]);
        }
        return password.toString();
    }
}
