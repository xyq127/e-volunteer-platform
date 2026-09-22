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
 * 志愿服务签到表 checkin，保存志愿者参与志愿服务的签到签退时间、签到码、定位轨迹与服务时长，
 * 签到位置或服务时长异常的记录会标记为待志愿者组织复核。
 */
@TableName(value ="checkin")
@Data
public class CheckIn implements Serializable {
    /** 签到编号 */
    @TableId(type = IdType.AUTO)
    private Integer checkinNum;

    /** 报名编号 */
    private Integer participateNum;

    /** 签到时间 */
    private Date checkinBegintime;

    /** 签退时间 */
    private Date checkinEndtime;

    /** 服务时长 */
    private Double checkinDuration;

    /** 服务时长复核状态 */
    private String checkinTimecheck;

    /** 签到使用的现场签到码 */
    private String checkinCode;

    /** 签到志愿者业务编号，志愿者组织复核时长时关联展示 */
    @TableField(exist = false)
    private String volunteerId;

    /** 签到志愿者姓名，志愿者组织复核时长时关联展示 */
    @TableField(exist = false)
    private String volunteerName;

    /** 签到志愿者联系电话，志愿者组织复核时长时关联展示 */
    @TableField(exist = false)
    private String volunteerTel;

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

    /** 复核时间 */
    private Date checkinChecktime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
