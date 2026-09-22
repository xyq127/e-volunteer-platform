package com.evolunteer.enums;

import lombok.Getter;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务技能标签枚举：志愿者档案与志愿活动申报共用的固定标签集合，
 * 标签一致是供需匹配计算的基础，避免同一技能因写法不同而无法匹配。
 */
@Getter
public enum VolunteerSkillEnum {

    STUDY_SUPPORT("助学支教"),
    ELDER_CARE("助老服务"),
    ENVIRONMENT_PROTECTION("环保公益"),
    COMMUNITY_SERVICE("社区服务"),
    MEDICAL_HEALTH("医疗健康"),
    EMERGENCY_RESCUE("应急救援"),
    LARGE_EVENT("大型赛会"),
    CULTURE_PROMOTION("文化宣传"),
    PSYCHOLOGICAL_CARE("心理疏导"),
    INFORMATION_TECHNOLOGY("信息技术");

    /**
     * 技能标签名称
     */
    private final String skillName;

    VolunteerSkillEnum(String skillName) {
        this.skillName = skillName;
    }

    /**
     * 校验技能标签是否属于平台固定的标签集合
     *
     * @param skillName 技能标签名称
     * @return 属于固定标签集合返回 true
     */
    public static boolean isSupported(String skillName) {
        if (skillName == null) {
            return false;
        }
        for (VolunteerSkillEnum skill : values()) {
            if (skill.skillName.equals(skillName.trim())) {
                return true;
            }
        }
        return false;
    }
}
