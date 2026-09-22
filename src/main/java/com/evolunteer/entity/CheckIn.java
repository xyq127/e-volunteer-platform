package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务记录表 checkin，保存志愿者参与志愿服务的签到签退时间、签到码、定位轨迹与服务时长。
 * 记录来源区分平台签到与志愿者组织补录：平台签到由志愿者组织复核，
 * 补录记录由平台管理员复核，避免录入与复核由同一方完成。
 */
@TableName(value ="checkin")
@Data
public class CheckIn implements Serializable {
    /** 签到编号 */
    @TableId(type = IdType.AUTO)
    private Integer checkinNum;

    /** 报名编号 */
    private Integer participateNum;

    /** 签到时间（服务开始时间） */
    private Date checkinBegintime;

    /** 签退时间（服务结束时间） */
    private Date checkinEndtime;

    /** 服务时长 */
    private Double checkinDuration;

    /** 服务时长复核状态 */
    private String checkinTimecheck;

    /** 记录来源，1 平台签到、2 志愿者组织补录 */
    private String checkinSource;

    /** 签到使用的现场签到码 */
    private String checkinCode;

    /** 签到地点纬度 */
    private BigDecimal checkinLatitude;

    /** 签到地点经度 */
    private BigDecimal checkinLongitude;

    /** 签到地点与活动地点的距离（米） */
    private Integer checkinDistance;

    /** 签退地点与活动地点的距离（米） */
    private Integer checkinOutdistance;

    /** 轨迹标记，1 正常、2 异常 */
    private String checkinFlag;

    /** 志愿者组织的复核意见 */
    private String checkinRemark;

    /** 平台管理员对补录记录的复核意见 */
    private String checkinAdminremark;

    /** 复核时间 */
    private Date checkinChecktime;

    /** 签到志愿者业务编号，志愿者组织复核时长时关联展示 */
    @TableField(exist = false)
    private String volunteerId;

    /** 签到志愿者姓名，志愿者组织复核时长时关联展示 */
    @TableField(exist = false)
    private String volunteerName;

    /** 签到志愿者联系电话，志愿者组织复核时长时关联展示 */
    @TableField(exist = false)
    private String volunteerTel;

    /** 签到志愿者编号，平台管理员复核补录时关联展示 */
    @TableField(exist = false)
    private Integer volunteerNum;

    /** 所属活动编号，平台管理员复核补录时关联展示 */
    @TableField(exist = false)
    private Integer activityNum;

    /** 所属活动名称，平台管理员复核补录时关联展示 */
    @TableField(exist = false)
    private String activityName;

    /** 所属活动编号（业务编号），平台管理员复核补录时关联展示 */
    @TableField(exist = false)
    private String activityId;

    /** 申报活动的志愿者组织名称，平台管理员复核补录时关联展示 */
    @TableField(exist = false)
    private String organizationName;

    /** 记录来源文案，用于页面展示 */
    @TableField(exist = false)
    private String checkinSourceText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
