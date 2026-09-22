package com.evolunteer.enums;

import lombok.Getter;

@Getter
public enum ActivityRejectReasonEnum {

    PLAN_INCOMPLETE("PLAN_INCOMPLETE", "活动方案不完整"),
    PEOPLE_UNCLEAR("PEOPLE_UNCLEAR", "人员安排不明确"),
    TIME_CONFLICT("TIME_CONFLICT", "活动时间或场地存在冲突"),
    INFO_MISSING("INFO_MISSING", "活动信息缺失"),
    SAFETY_RISK("SAFETY_RISK", "存在安全风险"),
    OTHER("OTHER", "其他原因");

    private final String reasonCode;

    private final String reasonName;

    ActivityRejectReasonEnum(String reasonCode, String reasonName) {
        this.reasonCode = reasonCode;
        this.reasonName = reasonName;
    }

    public static ActivityRejectReasonEnum of(String reasonCode) {
        if (reasonCode == null) {
            return null;
        }
        for (ActivityRejectReasonEnum reason : values()) {
            if (reason.reasonCode.equals(reasonCode.trim())) {
                return reason;
            }
        }
        return null;
    }
}
