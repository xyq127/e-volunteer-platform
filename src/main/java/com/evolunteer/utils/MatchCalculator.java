package com.evolunteer.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MatchCalculator {

    private static final int SKILL_SCORE = 40;

    private static final int DISTANCE_SCORE = 30;

    private static final int TIME_SCORE = 20;

    private static final int HISTORY_SCORE = 10;

    private static final int FULL_DISTANCE_SCORE_METERS = 1000;

    private static final int ZERO_DISTANCE_SCORE_METERS = 20000;

    private static final int NEUTRAL_SCORE = 15;

    private static final int DEFAULT_SKILL_SCORE = 20;

    private MatchCalculator() {
    }

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

    private static int timeScore(List<String> conflictActivityNames, List<String> reasons) {
        if (conflictActivityNames == null || conflictActivityNames.isEmpty()) {
            reasons.add("与您已报名的活动时间不冲突");
            return TIME_SCORE;
        }
        reasons.add("与您已报名的「" + String.join("」「", conflictActivityNames) + "」时间冲突");
        return 0;
    }

    private static int historyScore(int organizationHistoryCount, List<String> reasons) {
        if (organizationHistoryCount <= 0) {
            return 0;
        }
        reasons.add("您已参与该组织 " + organizationHistoryCount + " 次活动");
        return HISTORY_SCORE;
    }

    private static String formatKilometers(Integer distanceMeters) {
        return String.format("%.1f", distanceMeters / 1000.0);
    }

    public static class MatchResult {

        private int score;

        private List<String> matchedSkills;

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
