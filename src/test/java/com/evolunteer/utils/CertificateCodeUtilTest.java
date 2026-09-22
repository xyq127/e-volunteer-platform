package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务证明校验码工具类单元测试：校验码长度、稳定性与对数据变动的敏感性。
 */
class CertificateCodeUtilTest {

    /**
     * 测试使用的平台密钥
     */
    private static final String SECRET = "evolunteer-test-secret";

    @Test
    @DisplayName("校验码固定为 16 位大写十六进制字符串且可重复计算")
    void codeShouldBeStableHexString() {
        String code = CertificateCodeUtil.code(SECRET, "vol_00001", 3.0, 1);

        assertEquals(16, code.length());
        assertTrue(code.matches("[0-9A-F]{16}"));
        assertEquals(code, CertificateCodeUtil.code(SECRET, "vol_00001", 3.0, 1));
    }

    @Test
    @DisplayName("累计时长或服务记录数变化时校验码随之变化")
    void codeShouldChangeWithServiceData() {
        String code = CertificateCodeUtil.code(SECRET, "vol_00001", 3.0, 1);

        assertNotEquals(code, CertificateCodeUtil.code(SECRET, "vol_00001", 3.5, 1));
        assertNotEquals(code, CertificateCodeUtil.code(SECRET, "vol_00001", 3.0, 2));
        assertNotEquals(code, CertificateCodeUtil.code(SECRET, "vol_00002", 3.0, 1));
    }

    @Test
    @DisplayName("密钥不同时同一份服务数据的校验码不同，第三方无法伪造")
    void codeShouldDependOnSecret() {
        assertNotEquals(CertificateCodeUtil.code(SECRET, "vol_00001", 3.0, 1),
                CertificateCodeUtil.code("another-secret", "vol_00001", 3.0, 1));
    }

    @Test
    @DisplayName("累计时长为空时按 0 小时计算且不抛出异常")
    void codeShouldTolerateNullDuration() {
        String code = CertificateCodeUtil.code(SECRET, "vol_00001", null, null);

        assertEquals(16, code.length());
        assertEquals(code, CertificateCodeUtil.code(SECRET, "vol_00001", 0D, 0));
    }
}
