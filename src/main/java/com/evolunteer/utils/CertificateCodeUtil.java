package com.evolunteer.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public final class CertificateCodeUtil {

    private static final String ALGORITHM = "HmacSHA256";

    private static final int CODE_BYTES = 8;

    private CertificateCodeUtil() {
    }

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
