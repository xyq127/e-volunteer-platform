package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动表 activity，保存志愿者组织申报的志愿活动及其审核、开展状态。
 */
@TableName(value ="activity")
@Data
public class Activity implements Serializable {
    /** 活动编号 */
    @TableId(type = IdType.AUTO)
    private Integer activityNum;

    /** 组织编号 */
    private Integer organizationNum;

    /** 活动业务编号 */
    private String activityId;

    /** 活动名称 */
    private String activityName;

    /** 活动内容 */
    private String activityDetail;

    /** 活动开始时间 */
    private Date activityBegintime;

    /** 活动结束时间 */
    private Date activityEndtime;

    /** 活动地点 */
    private String activityLocation;

    /** 需求人数 */
    private Integer activityNeedpeople;

    /** 注意事项 */
    private String activityNotice;

    /** 发布时间 */
    private Date activityPublishtime;

    /** 报名截止时间 */
    private Date activitySignddl;

    /** 活动状态 */
    private String activityState;

    /** 管理员编号 */
    private Integer adminNum;

    /** 签到方式 */
    private String activityCheckin;

    /** 活动负责人编号 */
    private Integer activityLeaderid;

    /** 审核时间 */
    private Date activityChecktime;

    /** 审核意见 */
    private String activityRemark;

    /** 删除标记 */
    private Integer activityIsdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}