package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名表 participate，保存志愿者报名志愿活动的申请、审核、服务时长、参加确认与爽约信息。
 */
@TableName(value ="participate")
@Data
public class Participation implements Serializable {
    /** 报名编号 */
    @TableId(type = IdType.AUTO)
    private Integer participateNum;

    /** 志愿者编号 */
    private Integer volunteerNum;

    /** 活动编号 */
    private Integer activityNum;

    /** 报名申请时间 */
    private Date participateApplytime;

    /** 报名审核状态，0 待审核、1 已通过、2 未通过、3 候补 */
    private String participateApplystate;

    /** 报名审核时间 */
    private Date participateApplychecktime;

    /** 培训情况 */
    private String participateTraining;

    /** 服务开始时间 */
    private Date participateBegintime;

    /** 服务结束时间 */
    private Date participateEndtime;

    /** 服务时长 */
    private Double participateDuration;

    /** 服务时长审核状态 */
    private String participateTimecheck;

    /** 爽约标记，0 正常、1 爽约 */
    private Integer participateNoshow;

    /** 志愿者撤回报名时间 */
    private Date participateCancelTime;

    /** 参加确认状态，空 待确认、1 已确认、2 已放弃 */
    private String participateConfirmstate;

    /** 参加确认时间 */
    private Date participateConfirmtime;

    /** 删除标记 */
    @TableLogic
    private Integer participateIsdeleted;

    /**
     * 报名对应的志愿者信息，查询报名列表时关联填充
     */
    @TableField(exist = false)
    private Volunteer volunteer;

    /**
     * 志愿者与所报活动的匹配度，志愿者组织审核报名时参考，取值 0 至 100
     */
    @TableField(exist = false)
    private Integer matchScore;

    /**
     * 志愿者与所报活动的匹配理由，用于志愿者组织快速判断岗位适配度
     */
    @TableField(exist = false)
    private String matchReasons;

    /**
     * 参加确认状态文案，志愿者组织查看报名名单时展示
     */
    @TableField(exist = false)
    private String participateConfirmstateText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
