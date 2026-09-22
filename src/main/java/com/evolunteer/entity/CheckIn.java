package com.evolunteer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@TableName(value ="checkin")
@Data
public class CheckIn implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer checkinNum;

    private Integer participateNum;

    private Date checkinBegintime;

    private Date checkinEndtime;

    private Double checkinDuration;

    private String checkinTimecheck;

    private String checkinSource;

    private String checkinCode;

    private BigDecimal checkinLatitude;

    private BigDecimal checkinLongitude;

    private Integer checkinDistance;

    private Integer checkinOutdistance;

    private String checkinFlag;

    private String checkinRemark;

    private String checkinAdminremark;

    private String checkinAnomaly;

    private String checkinAnomalyremark;

    private String checkinObjectname;

    private String checkinObjectphone;

    private String checkinConfirmcode;

    private String checkinObjectconfirm;

    private Date checkinObjectconfirmtime;

    private String checkinObjectremark;

    private Date checkinChecktime;

    @TableField(exist = false)
    private String volunteerId;

    @TableField(exist = false)
    private String volunteerName;

    @TableField(exist = false)
    private String volunteerTel;

    @TableField(exist = false)
    private Integer volunteerNum;

    @TableField(exist = false)
    private Integer activityNum;

    @TableField(exist = false)
    private String activityName;

    @TableField(exist = false)
    private String activityId;

    @TableField(exist = false)
    private String organizationName;

    @TableField(exist = false)
    private String checkinSourceText;

    @TableField(exist = false)
    private String checkinObjectconfirmText;

    @TableField(exist = false)
    private String checkinAnomalyname;

    @TableField(exist = false)
    private String checkinTimecheckText;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
