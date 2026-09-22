package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者报名视图对象：志愿者工作台查询本人报名记录时，关联活动信息、签到信息与参加确认状态，
 * 并由业务层填充状态文案与可签到、可签退、可撤回、可确认等操作标记，不属于任何数据表。
 */
@Data
public class VolunteerParticipationView implements Serializable {
    /** 报名编号 */
    private Integer participateNum;

    /** 活动编号 */
    private Integer activityNum;

    /** 活动业务编号 */
    private String activityId;

    /** 活动名称 */
    private String activityName;

    /** 活动状态，0 审核中、1 未开始、2 进行中、3 已结束、4 审核未通过 */
    private String activityState;

    /** 活动开始时间 */
    private Date activityBegintime;

    /** 活动结束时间 */
    private Date activityEndtime;

    /** 活动地点 */
    private String activityLocation;

    /** 报名申请时间 */
    private Date participateApplytime;

    /** 报名审核状态，0 待审核、1 已通过、2 未通过、3 候补 */
    private String participateApplystate;

    /** 服务时长（小时） */
    private Double participateDuration;

    /** 服务时长复核状态，空 待复核、1 已确认、2 已驳回 */
    private String participateTimecheck;

    /** 参加确认状态，空 待确认、1 已确认、2 已放弃 */
    private String participateConfirmstate;

    /** 参加确认时间 */
    private Date participateConfirmtime;

    /** 签到时间 */
    private Date checkinBegintime;

    /** 签退时间 */
    private Date checkinEndtime;

    /** 签到核算出的服务时长（小时） */
    private Double checkinDuration;

    /** 签到地点与活动地点的距离（米） */
    private Integer checkinDistance;

    /** 轨迹标记，1 正常、2 异常 */
    private String checkinFlag;

    /** 服务时长复核状态，空 待复核、1 已确认、2 已驳回 */
    private String checkinTimecheck;

    /** 志愿者组织的复核意见 */
    private String checkinRemark;

    /** 记录来源，1 平台签到、2 志愿者组织补录 */
    private String checkinSource;

    /** 报名状态文案 */
    private String participateApplystateText;

    /** 参加确认状态文案 */
    private String participateConfirmstateText;

    /** 是否可签到 */
    private boolean canCheckin;

    /** 是否可签退 */
    private boolean canCheckout;

    /** 是否可撤回报名 */
    private boolean canCancel;

    /** 是否可确认参加 */
    private boolean canConfirm;

    private static final long serialVersionUID = 1L;
}
