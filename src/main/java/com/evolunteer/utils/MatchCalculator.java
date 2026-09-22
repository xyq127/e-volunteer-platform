package com.evolunteer.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配计算工具类：按技能匹配、位置距离、时间冲突与历史参与四个维度为志愿者与志愿活动打分，
 * 并给出可读的匹配理由。打分为纯函数计算，不依赖数据库与运行环境。
 */
public final class MatchCalculator {

    /**
     * 技能匹配维度满分
     */
    private static final int SKILL_SCORE = 40;

    /**
     * 距离维度满分
     */
    private static final int DISTANCE_SCORE = 30;

    /**
     * 时间维度满分
     */
    private static final int TIME_SCORE = 20;

    /**
     * 历史参与维度满分
     */
    private static final int HISTORY_SCORE = 10;

    /**
     * 距离满分的上限，志愿者常住地点与活动地点不超过该距离时距离维度得满分（米）
     */
    private static final int FULL_DISTANCE_SCORE_METERS = 1000;

    /**
     * 距离维度衰减到 0 的距离（米）
     */
    private static final int ZERO_DISTANCE_SCORE_METERS = 20000;

    /**
     * 缺乏技能、距离信息时给出的中性得分，避免无资料的活动被无谓降权
     */
    private static final int NEUTRAL_SCORE = 15;

    /**
     * 活动未设置技能要求时给出的技能维度得分
     */
    private static final int DEFAULT_SKILL_SCORE = 20;

    private MatchCalculator() {
    }

    /**
     * 计算志愿者与志愿活动的匹配度
     *
     * @param volunteerSkills         志愿者登记的服务技能标签
     * @param activitySkills          活动要求的服务技能标签
     * @param distanceMeters          志愿者常住地点与活动地点的距离（米），未知时传 null
     * @param conflictActivityNames   与活动时间冲突的已报名活动名称
     * @param organizationHistoryCount 志愿者在该活动所属组织已报名的活动数量
     * @return 匹配度计算结果，包含得分、命中的技能标签与匹配理由
     */
    public static MatchResult calculate(List<String> volunteerSkills,
                                        List<String> activitySkills,
                                        Integer distanceMeters,
                                        List<String> conflictActivityNames,
                                        int organizationHistoryCount) {

        List<String> matchedSkills = matchedSkills(volunteerSkills, activitySkills);
        List<String> reasons = new ArrayList<>();

        int score = skillScore(matchedSkills, activitySkills, reasons);
        score += distanceScore(distanceMeters, reasons);
        score += timeScore(conflictActivityNames, reasons);
        score += historyScore(organizationHistoryCount, reasons);

        MatchResult result = new MatchResult();
        result.setScore(Math.min(100, score));
        result.setMatchedSkills(matchedSkills);
        result.setReasons(reasons);
        return result;
    }

    /**
     * 计算志愿者与活动共同具备的技能标签，保持活动技能要求的顺序
     */
    private static List<String> matchedSkills(List<String> volunteerSkills, List<String> activitySkills) {
        List<String> matched = new ArrayList<>();
        if (volunteerSkills == null || activitySkills == null) {
            return matched;
        }
        Set<String> volunteerSkillSet = new LinkedHashSet<>(volunteerSkills);
        for (String skill : activitySkills) {
            if (skill != null && volunteerSkillSet.contains(skill) && !matched.contains(skill)) {
                matched.add(skill);
            }
        }
        return matched;
    }

    /**
     * 技能维度得分：按活动要求的技能标签命中比例折算
     */
    private static int skillScore(List<String> matchedSkills, List<String> activitySkills, List<String> reasons) {
        if (activitySkills == null || activitySkills.isEmpty()) {
            return DEFAULT_SKILL_SCORE;
        }
        int score = Math.round(SKILL_SCORE * matchedSkills.size() / (float) activitySkills.size());
        if (!matchedSkills.isEmpty()) {
            reasons.add("技能匹配：" + String.join("、", matchedSkills)
                    + "（命中 " + matchedSkills.size() + "/" + activitySkills.size() + " 项）");
        } else {
            reasons.add("活动要求技能：" + String.join("、", activitySkills) + "，与您的技能标签暂未重叠");
        }
        return score;
    }

    /**
     * 距离维度得分：1 公里内满分，2 万米外不计分，区间内线性衰减
     */
    private static int distanceScore(Integer distanceMeters, List<String> reasons) {
        if (distanceMeters == null) {
            reasons.add("未设置常住定位，按中性距离分值计算");
            return NEUTRAL_SCORE;
        }
        if (distanceMeters <= FULL_DISTANCE_SCORE_METERS) {
            reasons.add("距您 " + distanceMeters + " 米");
            return DISTANCE_SCORE;
        }
        if (distanceMeters >= ZERO_DISTANCE_SCORE_METERS) {
            reasons.add("距离较远（约 " + formatKilometers(distanceMeters) + " 公里）");
            return 0;
        }
        int score = Math.round(DISTANCE_SCORE
                * (ZERO_DISTANCE_SCORE_METERS - distanceMeters)
                / (float) (ZERO_DISTANCE_SCORE_METERS - FULL_DISTANCE_SCORE_METERS));
        reasons.add("距您约 " + formatKilometers(distanceMeters) + " 公里");
        return score;
    }

    /**
     * 时间维度得分：与已报名活动时间冲突时不计分
     */
    private static int timeScore(List<String> conflictActivityNames, List<String> reasons) {
        if (conflictActivityNames == null || conflictActivityNames.isEmpty()) {
            reasons.add("与您已报名的活动时间不冲突");
            return TIME_SCORE;
        }
        reasons.add("与您已报名的「" + String.join("」「", conflictActivityNames) + "」时间冲突");
        return 0;
    }

    /**
     * 历史参与维度得分：曾参与该组织活动说明服务偏好与协作基础较好
     */
    private static int historyScore(int organizationHistoryCount, List<String> reasons) {
        if (organizationHistoryCount <= 0) {
            return 0;
        }
        reasons.add("您已参与该组织 " + organizationHistoryCount + " 次活动");
        return HISTORY_SCORE;
    }

    /**
     * 将米转换为公里文案，保留一位小数
     */
    private static String formatKilometers(Integer distanceMeters) {
        return String.format("%.1f", distanceMeters / 1000.0);
    }

    /**
     * 匹配度计算结果：得分、命中的技能标签与匹配理由
     */
    public static class MatchResult {

        /** 匹配度，0 至 100 的整数 */
        private int score;

        /** 命中的技能标签 */
        private List<String> matchedSkills;

        /** 匹配理由 */
        private List<String> reasons;

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public void setMatchedSkills(List<String> matchedSkills) {
            this.matchedSkills = matchedSkills;
        }

        public List<String> getReasons() {
            return reasons;
        }

        public void setReasons(List<String> reasons) {
            this.reasons = reasons;
        }
    }
}
