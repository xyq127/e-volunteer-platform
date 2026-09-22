package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名表 participate，保存志愿者报名志愿活动的申请、审核与志愿服务时长信息。
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

    /** 报名审核状态 */
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

    /** 删除标记 */
    @TableLogic
    private Integer participateIsdeleted;

    /**
     * 报名对应的志愿者信息，查询报名列表时关联填充
     */
    @TableField(exist = false)
    private Volunteer volunteer;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}