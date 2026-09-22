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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台密码编码器：新密码使用 PBKDF2WithHmacSHA256 加盐摘要后存储，
 * 存储格式为 pbkdf2$迭代次数$盐$摘要，不同账号即使密码相同密文也不同。
 * <p>
 * 为兼容早期版本使用 MD5 摘要存储的账号，校验时先按历史格式比对，
 * 比对成功后由平台在登录过程中自动改写为加盐密文，历史账号无需手工重置密码。
 */
public class SecurePasswordEncoder implements PasswordEncoder {

    /**
     * 加盐摘要算法
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * 密文前缀，用于区分历史 MD5 密文
     */
    private static final String PREFIX = "pbkdf2";

    /**
     * 摘要迭代次数
     */
    private static final int ITERATIONS = 120000;

    /**
     * 盐长度（字节）
     */
    private static final int SALT_BYTES = 16;

    /**
     * 摘要长度（位）
     */
    private static final int HASH_BITS = 256;

    /**
     * 历史 MD5 密文格式：32 位小写十六进制
     */
    private static final Pattern LEGACY_PATTERN = Pattern.compile("[0-9a-f]{32}");

    /**
     * 生成加盐密码密文
     *
     * @param rawPassword 密码明文
     * @return pbkdf2$迭代次数$盐$摘要 形式的密文
     */
    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = digest(rawPassword, salt, ITERATIONS);

        Base64.Encoder encoder = Base64.getEncoder();
        return PREFIX + "$" + ITERATIONS + "$" + encoder.encodeToString(salt) + "$" + encoder.encodeToString(hash);
    }

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

    /**
     * 判断密码密文是否需要在登录成功后升级为加盐密文
     *
     * @param encodedPassword 数据库中保存的密码密文
     * @return 命中历史 MD5 格式时返回 true
     */
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return isLegacy(encodedPassword);
    }

    /**
     * 判断密码密文是否为历史 MD5 格式
     */
    private boolean isLegacy(String encodedPassword) {
        return encodedPassword != null && LEGACY_PATTERN.matcher(encodedPassword).matches();
    }

    /**
     * 按指定盐与迭代次数计算密码摘要
     */
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
