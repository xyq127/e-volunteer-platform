package com.evolunteer.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配计算工具类单元测试：校验技能、距离、时间与历史参与四个维度的得分与匹配理由。
 */
class MatchCalculatorTest {

    /**
     * 活动要求的技能标签
     */
    private static final List<String> ACTIVITY_SKILLS = Arrays.asList("社区服务", "文化宣传");

    @Test
    @DisplayName("技能全部命中且距离在 1 公里内、时间不冲突、有历史参与时匹配度为满分")
    void calculateShouldReturnFullScoreForPerfectMatch() {
        MatchCalculator.MatchResult result = MatchCalculator.calculate(
                Arrays.asList("社区服务", "文化宣传"), ACTIVITY_SKILLS, 500,
                Collections.emptyList(), 2);

        assertEquals(100, result.getScore());
        assertEquals(Arrays.asList("社区服务", "文化宣传"), result.getMatchedSkills());
        assertTrue(result.getReasons().contains("技能匹配：社区服务、文化宣传（命中 2/2 项）"));
        assertTrue(result.getReasons().contains("距您 500 米"));
        assertTrue(result.getReasons().contains("与您已报名的活动时间不冲突"));
        assertTrue(result.getReasons().contains("您已参与该组织 2 次活动"));
    }

    @Test
    @DisplayName("技能部分命中时按命中比例折算技能维度得分")
    void calculateShouldScoreSkillByHitRatio() {
        MatchCalculator.MatchResult result = MatchCalculator.calculate(
                Collections.singletonList("社区服务"), ACTIVITY_SKILLS, 1500,
                Collections.emptyList(), 0);

        // 技能 40 * 1/2 = 20，距离 30 * (20000-1500)/19000 ≈ 29，时间 20，历史 0
        assertEquals(69, result.getScore());
        assertEquals(Collections.singletonList("社区服务"), result.getMatchedSkills());
    }

    @Test
    @DisplayName("缺少定位时距离维度按中性分值计算并说明原因")
    void calculateShouldUseNeutralScoreWhenDistanceUnknown() {
        MatchCalculator.MatchResult result = MatchCalculator.calculate(
                Collections.emptyList(), Collections.emptyList(), null, Collections.emptyList(), 0);

        // 活动未设置技能要求 20，距离中性 15，时间不冲突 20
        assertEquals(55, result.getScore());
        assertTrue(result.getReasons().contains("未设置常住定位，按中性距离分值计算"));
    }

    @Test
    @DisplayName("时间冲突时时间维度不得分，并给出冲突活动名称")
    void calculateShouldPenalizeTimeConflict() {
        MatchCalculator.MatchResult result = MatchCalculator.calculate(
                ACTIVITY_SKILLS, ACTIVITY_SKILLS, 300, Collections.singletonList("社区义诊志愿服务"), 1);

        assertEquals(80, result.getScore());
        assertTrue(result.getReasons().contains("与您已报名的「社区义诊志愿服务」时间冲突"));
    }

    @Test
    @DisplayName("距离超过 2 万米时距离维度不得分且提示距离较远")
    void calculateShouldGiveZeroDistanceScoreWhenTooFar() {
        MatchCalculator.MatchResult result = MatchCalculator.calculate(
                Collections.emptyList(), ACTIVITY_SKILLS, 35000, Collections.emptyList(), 0);

        assertEquals(20, result.getScore());
        assertTrue(result.getReasons().contains("距离较远（约 35.0 公里）"));
    }

    @Test
    @DisplayName("技能标签为空或未登记技能时提示活动要求技能")
    void calculateShouldExplainMissingSkillOverlap() {
        MatchCalculator.MatchResult result = MatchCalculator.calculate(
                Collections.singletonList("信息技术"), ACTIVITY_SKILLS, 2000, Collections.emptyList(), 0);

        assertTrue(result.getMatchedSkills().isEmpty());
        assertTrue(result.getReasons().contains("活动要求技能：社区服务、文化宣传，与您的技能标签暂未重叠"));
    }
}
