package com.evolunteer.enums;

import lombok.Getter;

@Getter
public enum VolunteerStarEnum {

    NONE("未定级", 0, "一星志愿者"),
    ONE_STAR("一星志愿者", 100, "二星志愿者"),
    TWO_STAR("二星志愿者", 300, "三星志愿者"),
    THREE_STAR("三星志愿者", 600, "四星志愿者"),
    FOUR_STAR("四星志愿者", 1000, "五星志愿者"),
    FIVE_STAR("五星志愿者", 1500, null);

    private final String starName;

    private final double minHours;

    private final String nextStarName;

    VolunteerStarEnum(String starName, double minHours, String nextStarName) {
        this.starName = starName;
        this.minHours = minHours;
        this.nextStarName = nextStarName;
    }

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

    public static double hoursToNextStar(Double totalHours) {
        VolunteerStarEnum next = nextStar(totalHours);
        if (next == null) {
            return 0;
        }
        double hours = totalHours == null ? 0 : totalHours;
        return Math.max(0, next.minHours - hours);
    }

    public static VolunteerStarEnum nextStar(Double totalHours) {
        VolunteerStarEnum current = of(totalHours);
        if (current.nextStarName == null) {
            return null;
        }
        return values()[current.ordinal() + 1];
    }
}
