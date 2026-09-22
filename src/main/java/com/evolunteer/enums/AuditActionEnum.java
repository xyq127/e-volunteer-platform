package com.evolunteer.enums;

import lombok.Getter;

@Getter
public enum AuditActionEnum {

    ACTIVITY_AUDIT("ACTIVITY_AUDIT", "志愿活动审核"),
    ACTIVITY_REVISE("ACTIVITY_REVISE", "志愿活动重新申报"),
    ACTIVITY_DELETE("ACTIVITY_DELETE", "志愿活动删除"),
    ACTIVITY_STATE_CHANGE("ACTIVITY_STATE_CHANGE", "活动状态流转"),
    ACTIVITY_SETTLE("ACTIVITY_SETTLE", "活动考勤结算"),
    PARTICIPATE_CHECK("PARTICIPATE_CHECK", "报名审核"),
    PARTICIPATE_REMOVE("PARTICIPATE_REMOVE", "移除活动报名"),
    CHECKIN_REVIEW("CHECKIN_REVIEW", "服务时长复核"),
    MANUAL_HOUR_REVIEW("MANUAL_HOUR_REVIEW", "服务时长裁定"),
    ANOMALY_REVIEW("ANOMALY_REVIEW", "异常时长裁定"),
    SERVICE_CONFIRM_SHEET("SERVICE_CONFIRM_SHEET", "生成服务确认单"),
    SERVICE_OBJECT_CONFIRM("SERVICE_OBJECT_CONFIRM", "服务对象确认"),
    ACCOUNT_CREATE("ACCOUNT_CREATE", "开通组织账号"),
    ACCOUNT_ENABLED_CHANGE("ACCOUNT_ENABLED_CHANGE", "账号启停"),
    PASSWORD_RESET("PASSWORD_RESET", "重置账号密码"),
    PASSWORD_CHANGE("PASSWORD_CHANGE", "修改登录密码"),
    ANNOUNCEMENT_PUBLISH("ANNOUNCEMENT_PUBLISH", "发布通知公告"),
    ANNOUNCEMENT_DELETE("ANNOUNCEMENT_DELETE", "删除通知公告"),
    SHOW_DELETE("SHOW_DELETE", "删除志愿秀");

    private final String actionCode;

    private final String actionName;

    AuditActionEnum(String actionCode, String actionName) {
        this.actionCode = actionCode;
        this.actionName = actionName;
    }

    public static String nameOf(String actionCode) {
        for (AuditActionEnum action : values()) {
            if (action.actionCode.equals(actionCode)) {
                return action.actionName;
            }
        }
        return actionCode;
    }
}
