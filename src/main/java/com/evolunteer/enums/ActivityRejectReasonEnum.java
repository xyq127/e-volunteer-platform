package com.evolunteer.enums;

import lombok.Getter;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动审核不通过原因枚举：平台管理员驳回志愿活动时必须选择结构化原因，
 * 志愿者组织据此整改并修改后重新申报，避免出现只驳回不说明原因的情况。
 */
@Getter
public enum ActivityRejectReasonEnum {

    PLAN_INCOMPLETE("PLAN_INCOMPLETE", "活动方案不完整"),
    PEOPLE_UNCLEAR("PEOPLE_UNCLEAR", "人员安排不明确"),
    TIME_CONFLICT("TIME_CONFLICT", "活动时间或场地存在冲突"),
    INFO_MISSING("INFO_MISSING", "活动信息缺失"),
    SAFETY_RISK("SAFETY_RISK", "存在安全风险"),
    OTHER("OTHER", "其他原因");

    /**
     * 原因编码
     */
    private final String reasonCode;

    /**
     * 原因说明
     */
    private final String reasonName;

    ActivityRejectReasonEnum(String reasonCode, String reasonName) {
        this.reasonCode = reasonCode;
        this.reasonName = reasonName;
    }

    /**
     * 按原因编码查询枚举
     *
     * @param reasonCode 原因编码
     * @return 匹配的枚举，编码为空或不合法时返回 null
     */
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
