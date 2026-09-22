package com.evolunteer.enums;

import lombok.Getter;

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

    private final String skillName;

    VolunteerSkillEnum(String skillName) {
        this.skillName = skillName;
    }

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
