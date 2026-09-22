package com.evolunteer.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者星级枚举单元测试：校验星级认定阈值、成长进度与最高星级的边界。
 */
class VolunteerStarEnumTest {

    @Test
    @DisplayName("按累计服务时长认定星级，边界值归属上一档")
    void ofShouldMatchStarThresholds() {
        assertEquals("未定级", VolunteerStarEnum.of(0D).getStarName());
        assertEquals("未定级", VolunteerStarEnum.of(99.9).getStarName());
        assertEquals("一星志愿者", VolunteerStarEnum.of(100D).getStarName());
        assertEquals("二星志愿者", VolunteerStarEnum.of(300D).getStarName());
        assertEquals("三星志愿者", VolunteerStarEnum.of(600D).getStarName());
        assertEquals("四星志愿者", VolunteerStarEnum.of(1000D).getStarName());
        assertEquals("五星志愿者", VolunteerStarEnum.of(1500D).getStarName());
        assertEquals("五星志愿者", VolunteerStarEnum.of(9999D).getStarName());
        assertEquals("未定级", VolunteerStarEnum.of(null).getStarName());
    }

    @Test
    @DisplayName("计算距离下一星级还需的累计服务时长")
    void hoursToNextStarShouldReturnRemainingHours() {
        assertEquals(100D, VolunteerStarEnum.hoursToNextStar(0D));
        assertEquals(97D, VolunteerStarEnum.hoursToNextStar(3D), 0.001);
        assertEquals(0D, VolunteerStarEnum.hoursToNextStar(1500D));
    }

    @Test
    @DisplayName("下一星级在已达最高星级时为空")
    void nextStarShouldBeNullForTopStar() {
        assertEquals(VolunteerStarEnum.ONE_STAR, VolunteerStarEnum.nextStar(99D));
        assertEquals(VolunteerStarEnum.FIVE_STAR, VolunteerStarEnum.nextStar(1200D));
        assertNull(VolunteerStarEnum.nextStar(1500D));
    }
}
