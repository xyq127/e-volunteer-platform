package com.evolunteer.utils;

import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

public class SecurePasswordEncoder implements PasswordEncoder {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    private static final String PREFIX = "pbkdf2";

    private static final int ITERATIONS = 120000;

    private static final int SALT_BYTES = 16;

    private static final int HASH_BITS = 256;

    private static final Pattern LEGACY_PATTERN = Pattern.compile("[0-9a-f]{32}");

    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = digest(rawPassword, salt, ITERATIONS);

        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (isLegacy(encodedPassword)) {
            return MessageDigest.isEqual(
                    encodedPassword.getBytes(StandardCharsets.UTF_8),
                    MD5Util.encode(rawPassword.toString()).getBytes(StandardCharsets.UTF_8));
        }

        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, digest(rawPassword, salt, iterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return isLegacy(encodedPassword);
    }

    private boolean isLegacy(String encodedPassword) {
        return encodedPassword != null && LEGACY_PATTERN.matcher(encodedPassword).matches();
    }

    private byte[] digest(CharSequence rawPassword, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toString().toCharArray(), salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("当前运行环境不支持 " + ALGORITHM + " 摘要算法", e);
        } finally {
            spec.clearPassword();
        }
    }
}
