package com.evolunteer.enums;

import lombok.Getter;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者星级枚举：按已核定累计服务时长认定星级，用于志愿者成长激励与服务证明展示。
 * 时长阈值参照各地志愿服务星级认定标准：100、300、600、1000、1500 小时分别对应一至五星。
 */
@Getter
public enum VolunteerStarEnum {

    NONE("未定级", 0, "一星志愿者"),
    ONE_STAR("一星志愿者", 100, "二星志愿者"),
    TWO_STAR("二星志愿者", 300, "三星志愿者"),
    THREE_STAR("三星志愿者", 600, "四星志愿者"),
    FOUR_STAR("四星志愿者", 1000, "五星志愿者"),
    FIVE_STAR("五星志愿者", 1500, null);

    /**
     * 星级名称
     */
    private final String starName;

    /**
     * 认定该星级所需的最小累计服务时长（小时）
     */
    private final double minHours;

    /**
     * 下一星级名称，已是最高星级时为空
     */
    private final String nextStarName;

    VolunteerStarEnum(String starName, double minHours, String nextStarName) {
        this.starName = starName;
        this.minHours = minHours;
        this.nextStarName = nextStarName;
    }

    /**
     * 按累计服务时长认定星级
     *
     * @param totalHours 已核定累计服务时长（小时）
     * @return 星级枚举，未达到任何星级时返回未定级
     */
    public static VolunteerStarEnum of(Double totalHours) {
        double hours = totalHours == null ? 0 : totalHours;
        VolunteerStarEnum current = NONE;
        for (VolunteerStarEnum star : values()) {
            if (hours >= star.minHours) {
                current = star;
            }
        }
        return current;
    }

    /**
     * 计算距离下一星级还需要累计的服务时长
     *
     * @param totalHours 已核定累计服务时长（小时）
     * @return 距离下一星级所需的剩余时长，已达最高星级时返回 0
     */
    public static double hoursToNextStar(Double totalHours) {
        VolunteerStarEnum next = nextStar(totalHours);
        if (next == null) {
            return 0;
        }
        double hours = totalHours == null ? 0 : totalHours;
        return Math.max(0, next.minHours - hours);
    }

    /**
     * 获取累计服务时长对应的下一星级
     *
     * @param totalHours 已核定累计服务时长（小时）
     * @return 下一星级枚举，已达最高星级时返回 null
     */
    public static VolunteerStarEnum nextStar(Double totalHours) {
        VolunteerStarEnum current = of(totalHours);
        if (current.nextStarName == null) {
            return null;
        }
        return values()[current.ordinal() + 1];
    }
}
