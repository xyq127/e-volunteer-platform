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
 * 志愿服务签到表 checkin，保存志愿者参与志愿服务的签到时间与服务时长。
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

    /** 服务时长审核状态 */
    private String checkinTimecheck;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}