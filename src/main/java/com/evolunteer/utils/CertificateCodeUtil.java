package com.evolunteer.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务证明校验码工具类：以平台密钥对志愿者的累计服务数据计算 HMAC-SHA256 摘要，
 * 取前 8 字节转为 16 位大写十六进制校验码。数据一旦变化校验码随之变化，
 * 第三方校验时重新计算即可判断证明是否被篡改。
 */
public final class CertificateCodeUtil {

    /**
     * 生成校验码使用的摘要算法
     */
    private static final String ALGORITHM = "HmacSHA256";

    /**
     * 校验码字节数，对应 16 位十六进制字符
     */
    private static final int CODE_BYTES = 8;

    private CertificateCodeUtil() {
    }

    /**
     * 计算志愿服务证明校验码
     *
     * @param secret        平台证明密钥
     * @param volunteerId   志愿者业务编号
     * @param totalDuration 已核定累计服务时长（小时）
     * @param activityCount 已核定服务记录数
     * @return 16 位大写十六进制校验码
     */
    public static String code(String secret, String volunteerId, Double totalDuration, Integer activityCount) {

        String payload = volunteerId + "|" + String.format("%.1f", totalDuration == null ? 0D : totalDuration)
                + "|" + (activityCount == null ? 0 : activityCount);

        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder code = new StringBuilder();
            for (int i = 0; i < CODE_BYTES; i++) {
                code.append(String.format("%02X", digest[i]));
            }
            return code.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("当前运行环境不支持 " + ALGORITHM + " 摘要算法", e);
        }
    }
}
